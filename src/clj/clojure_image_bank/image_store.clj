(ns clojure-image-bank.image-store
  (:require [environ.core :refer [env]]
            [clojure.java.io    :as io]
            [clojure.string :as s]
            [clojure.pprint :refer [pprint pp]]
            ;; -------
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]
            [clj-exif.core :as exif]
            [clj-time.core :as t]
            [clj-time.format :as tf]))

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

;; -------------------------------------------------------

(defn get-exif-metadata
  [file]
  (let [metadata (exif/get-metadata file)]
    (if metadata (exif/read metadata))))

(def exif-date-format "yyyy:MM:dd HH:mm:ss")

(defn get-date-time
  "EXIF time is in format: 2003:12:14 12:01:44"
  [metadata]
  (tf/parse (tf/formatter exif-date-format)
            (get-in metadata ["Root" "DateTime"])))

(defn get-orientation
  [metadata]
  (get-in metadata ["Root" "Orientation"]))

;; -------------------------------------------------------

(defn is-jpeg [file]
  (s/ends-with? (s/lower-case file) ".jpg"))

(defn images-to-import
  "JPG image files in the media/import directory"
  []
  (filter is-jpeg (file-seq (media-path "_import"))))

(defn media-path-for-image
  "Image path is Year/Month/Day/Filename based on DateTime in EXIF data"
  [image-file]
  (let [d (get-date-time (get-exif-metadata image-file))]
    (media-path (str (t/year d))
                (str (t/month d))
                (str (t/day d))
                (.getName image-file))))

(defn move-image-into-store
  "Move image from import into store"
  [image-file]
  (let [destination (media-path-for-image image-file)]
    (.mkdirs (.getParentFile destination))
    (.renameTo image-file destination)
    destination))

(defn make-image-thumbnail
  "Make a small version for browsing, call after move-image-into-store"
  [image-file]
  (let [destination (thumbnail-file image-file)]
    (.mkdirs (.getParentFile destination))
    (let [thumbnail (resize image-file 100 100)]
      (format/as-file thumbnail (str destination) :verbatim))))

(defn import-images
  "Process images and store them away.
  TODO: store metadata somewhere
  FIX: what about images that have the same name (already imported?)
  FIX: what about images that have no EXIF data?"
  []
  (map #(make-image-thumbnail (move-image-into-store %))
       (images-to-import)))

;; -------------------------------------------------------

(defn get-directories
  [path]
  (map #(.getName %)
       (filter #(.isDirectory %) (.listFiles path))))

(defn get-photos
  "Return a list of JPEGs in this path"
  [path]
  (filter is-jpeg (.listFiles path)))

(defn top-level-categories []
  "Return a list of directory names as strings"
  (filter #(not (s/starts-with? % "_"))
          (get-directories (media-path))))

(defn categories [category]
  "Return a list of directory names within this category"
  (get-directories (media-path category)))

(defn photos [category]
  "Return a list of photo Files"
  (get-photos (media-path category)))

