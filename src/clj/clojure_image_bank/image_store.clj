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

(defn media-path-string
  "As media-path, but as a string"
  [& path]
  (.getPath (apply media-path path)))

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

(defn images-to-import
  "JPG image files in the media/import directory"
  []
  (filter #(s/ends-with? (s/lower-case %) ".jpg")
          (file-seq (media-path "import"))))

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
    (.renameTo image-file destination)))

(defn import-images
  "Process images and store them away.
  TODO: store metadata somewhere
  FIX: what about images that have the same name (already imported?)
  FIX: what about images that have no EXIF data?"
  []
  (map #(move-image-into-store %)
       (images-to-import)))

;; -------------------------------------------------------

(defn get-years []
  (filter #(.isDirectory %)
          (file-seq (media-path))))
