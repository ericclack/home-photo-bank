(ns clojure-image-bank.test.image
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [clojure-image-bank.handler :refer :all]
            [clojure-image-bank.image-store :as imgs]
            ;;---
            [clojure.java.io    :as io]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]
            [clj-exif.core :as exif]
            [clj-time.core :as t]
            [clj-time.format :as tf]))


;; -----------------------------------------------------

(deftest test-image
  (testing "resize"
    (let [file (imgs/media-path "test" "boat.jpg")
          thumbnail (resize file 100 100)
          outfilepath (format/as-file thumbnail (imgs/media-path-string "out.jpg"))]
      (is (s/ends-with? outfilepath (imgs/media-path-string "out_100x100.jpg"))))))

(deftest test-exif
  (testing "Exif data from images"
    (let [file (imgs/media-path "test" "flower_exiv2.jpg")
          metadata (imgs/get-exif-metadata file)]
      (is (= 1 (imgs/get-orientation metadata)))
      (is (= 2003 (t/year (imgs/get-date-time metadata)))))))
