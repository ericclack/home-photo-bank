(ns clojure-image-bank.test.image
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [clojure-image-bank.handler :refer :all]
            [clojure.java.io    :as io]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]
            [clj-exif.core :as exif]
            [clj-time.core :as t]
            [clj-time.format :as tf]))

(defn media-file
  [& path]
  (apply io/file (cons (env :media-path) path)))

(defn media-path
  [& path]
  (.getPath (apply media-file path)))

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

;; -----------------------------------------------------

(deftest test-image
  (testing "resize"
    (let [file (media-file "test" "boat.jpg")
          thumbnail (resize file 100 100)
          outfilepath (format/as-file thumbnail (media-path "out.jpg"))]
      (is (s/ends-with? outfilepath (media-path "out_100x100.jpg"))))))

(deftest test-exif
  (testing "Exif data from images"
    (let [file (media-file "test" "flower_exiv2.jpg")
          metadata (get-exif-metadata file)]
      (is (= 1 (get-orientation metadata)))
      (is (= 2003 (t/year (get-date-time metadata)))))))
