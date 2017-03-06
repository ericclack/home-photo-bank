(ns clojure-image-bank.test.image
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [clojure-image-bank.handler :refer :all]
            [clojure.java.io    :as io]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]
            ;;[exif-processor.core :as exif]
            [clj-exif.core :as exif]
            ))

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

(defn get-date-time
  [metadata]
  (get-in metadata ["Root" "DateTime"]))

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
      (is (s/starts-with? (get-date-time metadata) "2003:12:14"))))) 
