(ns home-photo-bank.photo-store
  "Store and retrive photos in categories. Categories
  are nested folders in the file system, which could
  be anything (needs testing), but commonly year/month/day.

  Special directories:
  _import: photos ready to import
  _process: photos that need processing before import
  _failed: photos that failed to import
  _thumbs: thumbnails for imported photos.

  Guarantees:
  This module will never modify files. It will rename
  them both to move to storage folders and to add keywords
  but only when moving from _process to _import.
  "
  (:require [environ.core :refer [env]]
            [clojure.java.io    :as io]
            [clojure.string :as s]
            [clojure.pprint :refer [pprint pp]]
            [clojure.tools.logging :as log]
            ;; -------
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]
            [clj-exif.core :as exif]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [digest :as digest]
            ;; -------
            [home-photo-bank.constants :as const]
            [home-photo-bank.models.db :as db]
            [home-photo-bank.shell :as shell])
  
  (:import [java.util.zip ZipEntry ZipOutputStream]))

(def thumbnail-size 300) ;; bounding box 300x300

;; -------------------------------------------------------

(defn media-path
  "A file object for this path inside the media directory.
  Path is expressed as a list of directories and an optional file"
  [& path]    
  (apply io/file (cons (env :media-path) path)))

(defn thumbnail-file
  "Return the (location of the) thumbnail for this file"
  [file]
  (io/file (s/replace (str file)
                      (env :media-path)
                      (str (env :media-path) "/_thumbs"))))

(defn split-extension
  "Return list of (name extension)"
  [file]
  (s/split (.getName file) #"\."))


;; -------------------------------------------------------

(defn get-exif-metadata
  [file]
  (let [metadata (exif/get-metadata file)]
    (if metadata (exif/read metadata))))

(defn has-date-created? [metadata]
  (let [date-created (get-in metadata ["Exif" "DateTimeOriginal"])]
    (and (some? date-created)
         (not= date-created const/exif-null-date))))

(defn get-date-created
  "EXIF time is in format: 2003:12:14 12:01:44"
  [metadata]
  (tf/parse const/exif-formatter
            (get-in metadata ["Exif" "DateTimeOriginal"])))

(defn get-exif-date-created [file]
  (get-date-created (get-exif-metadata file)))

(defn get-orientation
  [metadata]
  (get-in metadata ["Root" "Orientation"]))

(defn set-exif-date-created!
  "Set date-created with a string in
  format 2006-06-01T10:11"
  [file date-created]
  (shell/set-exif-date-created! file date-created))

;; -------------------------------------------------------

(defn get-digest
  [file]
  (digest/sha-256 file))

(defn maybe-duplicate?
  "Check to see if we have seen this digest before. If we have this photo is likely to be a duplicate."
  [file]
  (let [digest (get-digest file)]
    (db/photos-with-digest digest)))

;; -------------------------------------------------------

(defn is-jpeg [file]
  (s/ends-with? (s/lower-case file) ".jpg"))

(defn photos-to-import
  "JPG photo files in the media/_import directory"
  []
  (filter is-jpeg (file-seq (media-path "_import"))))

(defn media-path-for-photo
  "Photo path is Year/Month/Day/Filename based on DateTime in EXIF data"
  [photo-file]
  (let [d (get-date-created (get-exif-metadata photo-file))]
    (media-path (str (t/year d))
                (str (t/month d))
                (str (t/day d))
                (.getName photo-file))))

(defn file-name-to-keywords
  "Keywords from file name (without extension), 
  separated by -, multi-word separated by _. Single 
  letter keywords are ignored."
  [name]
  (filter #(> (count %) 1)
          (map #(s/replace % "_" " ")
               (s/split (s/lower-case name) #"[ \-,]"))))

(defn keywords-to-file-name
  "Turn a list of keywords into a file name (without extension)"
  [keywords]
  (s/lower-case (s/replace (s/join "-" keywords)
                           " " "_")))

(defn make-photo-metadata [photo]
  (let [path (str photo)
        filename (.getName photo)
        name (first (split-extension photo))]
    {:_id path
     :path path
     :filename filename
     :name name
     :datetime (get-date-created (get-exif-metadata photo))
     :category (s/replace (.getParent photo)
                          (str (env :media-path) "/")
                          "")
     :keywords (file-name-to-keywords name)
     :digest (get-digest photo)
     }))

(defn move-photo-into-store!
  "Move photo from import into store"
  [photo-file]
  (let [destination (media-path-for-photo photo-file)]
    (.mkdirs (.getParentFile destination))
    (.renameTo photo-file destination)
    destination))

(defn move-photo-into-failed!
  [photo-file]
  (let [destination (media-path "_failed" (.getName photo-file))]
    (.renameTo photo-file destination)
    destination))

(defn resized-file-as-stream
  [photo-file size]
  (format/as-stream 
   (resize photo-file size size)
   "jpg"))

(defn make-photo-thumbnail!
  "Make a small version for browsing, call after move-photo-into-store"
  [photo-file]
  (let [destination (thumbnail-file photo-file)]
    (.mkdirs (.getParentFile destination))
    (let [thumbnail (resize photo-file thumbnail-size thumbnail-size)]
      (format/as-file thumbnail (str destination) :verbatim))))

(defn import-photo! 
  [photo-file]
  (try
    (let [stored-photo (move-photo-into-store! photo-file)]
      (make-photo-thumbnail! stored-photo)
      (db/set-photo-metadata! (make-photo-metadata stored-photo))
      stored-photo)
    (catch Exception e
      (log/warn "Cannot import" photo-file "Maybe missing EXIF?" e)
      (move-photo-into-failed! photo-file))))

(defn import-photos!
  "Process photos and store them away."  
  []
  (map import-photo! (photos-to-import)))

(defn regen-thumbnails!
  [path]
  (map #(make-photo-thumbnail! %)
       (filter is-jpeg 
               (file-seq path))))

(defn watch-and-import!
  "Repeatedly watch for, then import photos."
  ([] (watch-and-import! 2))
  ([minutes-sleep]
   (when-let [imports (not-empty (import-photos!))]
     (log/info "imported photos" imports))
   (Thread/sleep (* minutes-sleep 60 1000))
   (recur minutes-sleep)))

;; -------------------------------------------------------

(defn get-directories
  [path]
  (sort (map #(.getName %)
             (filter #(.isDirectory %) (.listFiles path)))))

(defn get-photos
  "Return a list of JPEGs in this path"
  [path]
  (filter is-jpeg (.listFiles path)))

(defn top-level-categories []
  "Return a list of directory names as strings"
  (filter #(not (s/starts-with? % "_"))
          (get-directories (media-path))))

(defn categories 
  "Return a sorted list of directory names within this category.
  Assumes that directory names are numeric."
  [category]
  (sort #(compare (Integer. %1) (Integer. %2)) 
        (get-directories (media-path category))))

(defn photos-in-category [category]
  "Return a list of photo Files"
  (get-photos (media-path category)))

;; --

(defn month-name [month]
  (tf/unparse (tf/formatter "MMMM")
              (t/date-time 2017 month 1)))

(defn category-name [category]
  "Currently just the month name and year"
  (let [ymd (re-matches #"^(\d\d\d\d)/(\d+)/(\d+)$" category)
        ym (re-matches #"^(\d\d\d\d)/(\d+)$" category)
        y (re-matches #"^(\d\d\d\d)$" category)]
    (cond
      (some? ymd) (str (nth ymd 3) " "
                       (month-name (Integer/parseInt (nth ymd 2)))
                       " " (nth ymd 1))
      (some? ym)  (str (month-name (Integer/parseInt (nth ym 2))) " "
                       (nth ym 1))
      (some? y)   (str (nth y 1))
      )))

(defn category-sort-key
  "Zero pad single digit months and days"
  [category]
  (s/replace 
   (s/replace category #"^(\d\d\d\d)/(\d)/" "$1/0$2/")
   #"^(\d\d\d\d)/(\d\d)/(\d)$" "$1/$2/0$3"))

(defn sort-categories
  "Sort list of categories in date order"
  [categories]
  (sort #(compare (category-sort-key %1) (category-sort-key %2))
                  categories))

(defn categories-and-names [category]
  "A list of pairs: '(category name)"
  (map #(list % (category-name (str category "/" %)))
       (categories category)))

(defn date-parts-to-category [parts]
  "Return string form of category from date parts"
  (s/join "/" (filter some? parts)))

(defn next-month-category [year month]
  (date-parts-to-category
   (if (= 12 month)
     (list (+ 1 year) 1)
     (list year (+ 1 month)))))

(defn prev-month-category [year month]
  (date-parts-to-category
   (if (= 1 month)
     (list (- year 1) 12)
     (list year (- month 1)))))

;; -------------------------------------------------------

(defn all-photos 
  "Return all photos across all top-level categories
  from the file system, not couch metadata."
  []
  (filter is-jpeg
          (flatten
           (map #(file-seq (media-path %))
                (top-level-categories)))))  

(defn create-initial-photo-metadata! []
  (map
   #(try
      (db/set-photo-metadata! (make-photo-metadata %))
      (catch Exception e (log/warn (.getMessage e))))
   (reverse (all-photos))))

;; -------------------------------------------------------

(defn photos-to-process
  "JPG photo files in the media/_process directory"
  []
  (sort #(compare (.lastModified %1) (.lastModified %2))
        (filter is-jpeg (file-seq (media-path "_process")))))

(defn process-photo-add-keywords!
  "Add keywords to this photo by renaming it, ready
  for import. Seq is 1 unless the new name would clash
  with an existing file, in which case it is incremented."
  ([photo-file keywords] (process-photo-add-keywords! photo-file keywords 1))
  ([photo-file keywords seq]
   (let [path (.getParentFile photo-file)
         old-name (.getName photo-file)
         keywords-part (keywords-to-file-name keywords)
         extension (second (split-extension photo-file))
         new-name (str keywords-part "-" seq "." extension)
         new-file (io/file path new-name)]
     (when-not (= old-name new-name)
       (if (.exists new-file)
         (recur photo-file keywords (+ 1 seq))
         (.renameTo photo-file new-file))))))

(defn has-keywords?
  "We guess this file has been keyworded if its name ends 
  with -nnn and it has a multi-char strings in it"
  [file]
  (re-find #"[a-zA-Z]{2,}.*-\d+" (first (split-extension file))))

(defn process-photos-with-no-keywords
  []
  (remove has-keywords?
          (photos-to-process)))

(defn processed-photos-with-keywords
  []
  (filter has-keywords?
          (photos-to-process)))

(defn move-process-photo-to-import!
  [file]
  (let [destination (media-path "_import" (.getName file))]
    (.renameTo file destination)
    destination))  

(defn move-processed-to-import!
  []
  (map move-process-photo-to-import!
       (processed-photos-with-keywords)))

;; -------------------------------------------------------

(defn selected-photos-as-zip
  "Write the selected photos as a zip to an output-stream,
   flattening the media directory structure to make it easier
   to work with the resulting files.
   E.g.
   (selected-photos-as-zip 
      (io/output-stream \"foo.zip\"))"  
  [out-stream]
  (with-open [zip (ZipOutputStream. out-stream)]
    (doseq [p (db/photos-selected "1")]
      (let [path (:path p)
            flattend-path (s/replace path "/" ",")]
        (.putNextEntry zip (ZipEntry. flattend-path))
        (io/copy (io/file path) zip)
        (.closeEntry zip))))
  )

;; -------------------------------------------------------

(defn fix-photos-without-metadatum!
  "Find and fix photos without this metadata, value-getter takes a media-path file and returns the appropriate value"
  [metadata-key value-getter]
  
  ;; To fix missing datetime, run with
  ;; (fix-photos-without-metadatum! "datetime" get-exif-date-created)
  ;; To fix missing digest run with
  ;; (fix-photos-without-metadatum! "digest" get-digest)
  
  (defn fix-photo-without-metadatum!
    [photo-path]
    (try
      ;; photo-path starts with media/ which will result in
      ;; incorrect path media/media/... - so we need to remove it
      ;; and fix in code at somepoint, see make-photo-metadata
      (let [pp (s/replace photo-path #"^media/" "")
            value (value-getter (media-path pp))]
        (db/update-photo-metadata! photo-path
                                   metadata-key value))
      (catch java.io.FileNotFoundException e
        (log/warn "Cannot fix metadata for" photo-path
                  "Maybe missing file?" (.getMessage e)))
      (catch Exception e
        (log/error "Cannot fix metadata for" photo-path "Cause: " e))))
  
  (map fix-photo-without-metadatum!
       (map :path
            (take 1000
                  (db/photos-without-metadatum metadata-key)))))

