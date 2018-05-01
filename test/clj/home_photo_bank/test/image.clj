(ns home-photo-bank.test.image
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [home-photo-bank.handler :refer :all]
            [home-photo-bank.photo-store :as ps]
            [clojure.tools.logging :as log]
            ;;---
            [clojure.java.io    :as io]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]
            [clj-exif.core :as exif]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [exif-processor.core :as exifp]))


;; -----------------------------------------------------

(deftest test-image
  (testing "resize"
    (let [file (ps/media-path "_test" "boat.jpg")
          thumbnail (resize file 100 100)
          outfilepath (format/as-file thumbnail (ps/media-path-string "out.jpg"))]
      (is (s/ends-with? outfilepath (ps/media-path-string "out_100x100.jpg"))))))

(deftest test-exif
  (testing "Exif data from images"
    (let [file (ps/media-path "_test" "flower_exiv2.jpg")
          metadata (ps/get-exif-metadata file)]
      (is (= 1 (ps/get-orientation metadata)))
      (is (= 2003 (t/year (ps/get-date-time metadata)))))))
