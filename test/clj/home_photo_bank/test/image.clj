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
            [clj-time.core :as t]
            [clj-time.format :as tf]))


;; -----------------------------------------------------
;; In Cider run tests with C-c C-t C-t

(deftest test-image
  (testing "resize"
    (let [file (ps/media-path "_test" "boat.jpg")
          thumbnail (resize file 100 100)
          outfilepath (format/as-file thumbnail (.getPath (ps/media-path "out.jpg")))]
      (is (s/ends-with? outfilepath (.getPath (ps/media-path "out_100x100.jpg")))))))

(deftest test-exif
  (testing "Exif data from images"
    (let [file (ps/media-path "_test" "flower_exiv2.jpg")
          metadata (ps/get-exif-metadata file)]
      (is (= 2003 (t/year (ps/get-date-created metadata)))))))

(deftest metadata
  (testing "date-metadata"
    (let [file (ps/media-path "_test" "flower_exiv2.jpg")
          metadata (ps/make-photo-metadata file)]
      (is (some? (metadata :datetime)))))
  (testing "artist-metadata"
    (let [file1 (ps/media-path "_test" "sky.jpeg")
          metadata1 (ps/make-photo-metadata file1)
          file2 (ps/media-path "_test" "flower_exiv2.jpg")
          metadata2 (ps/make-photo-metadata file2)]
      (is (some? (metadata1 :artist)))
      (is (nil? (metadata2 :artist)))))
  )

(deftest exif-metadata
  (testing "gps-exif-metadata"
    (let [file (ps/media-path "_test" "sky.jpeg")
          metadata (ps/get-exif-metadata file)]
      (is (= (metadata "GPS Latitude") "50° 53' 53.67\""))
      (is (= (metadata "GPS Latitude Ref") "N"))
      (is (= (metadata "GPS Longitude") "0° 4' 15.64\""))
      (is (= (metadata "GPS Longitude Ref") "W"))
         )))

(deftest gps-metadata
  (testing "gps-metadata-dms"
    (let [file (ps/media-path "_test" "sky.jpeg")
          dms-pair (ps/get-gps-location-dms file)]
      (is (= (first dms-pair) '(50 53 53.67 "N")))
      (is (= (second dms-pair) '(0 4 15.64 "W")))
          ))
  (testing "gps-metadata-coords"
    (let [file (ps/media-path "_test" "sky.jpeg")
          coord-pair (ps/get-gps-location file)]
      (is (some? (first coord-pair))) ; 50.89824167))
      (is (some? (second coord-pair))) ; 0.07101111))
          )))


