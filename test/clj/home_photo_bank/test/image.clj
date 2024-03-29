(ns home-photo-bank.test.image
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [home-photo-bank.handler :refer :all]
            [home-photo-bank.photo-store :as ps]
            [home-photo-bank.exif :as ex]            
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
    (let [file (ps/media-path "_test" "flower_exif2.jpg")
          metadata (ex/get-metadata file)]
      (is (= 2003 (t/year (ex/get-date-created metadata)))))))

(deftest metadata
  (testing "date-metadata"
    (let [file (ps/media-path "_test" "flower_exif2.jpg")
          metadata (ps/make-photo-metadata file)]
      (is (some? (metadata :datetime)))))
  (testing "artist-metadata"
    (let [file1 (ps/media-path "_test" "sky.jpeg")
          metadata1 (ps/make-photo-metadata file1)
          file2 (ps/media-path "_test" "flower_exif2.jpg")
          metadata2 (ps/make-photo-metadata file2)]
      (is (some? (metadata1 :artist)))
      (is (nil? (metadata2 :artist)))))
  )

(deftest exif-metadata
  (testing "gps-exif-metadata"
    (let [file (ps/media-path "_test" "sky.jpeg")
          metadata (ex/get-metadata file)]
      (is (= (metadata "GPS Latitude") "50° 53' 53.67\""))
      (is (= (metadata "GPS Latitude Ref") "N"))
      (is (= (metadata "GPS Longitude") "0° 4' 15.64\""))
      (is (= (metadata "GPS Longitude Ref") "W"))
         )))

(deftest gps-metadata
  (testing "no-gps-metadata"
    (let [file (ps/media-path "_test" "flower_exif2.jpg")
          dms-pair (ex/get-gps-location-dms file)]
      (is (nil? dms-pair))))
  (testing "gps-metadata-dms"
    (let [file (ps/media-path "_test" "sky.jpeg")
          dms-pair (ex/get-gps-location-dms file)]
      (is (= (first dms-pair) '(50 53 53.67 "N")))
      (is (= (second dms-pair) '(0 4 15.64 "W")))))
  (testing "gps-metadata-coords"
    (let [file (ps/media-path "_test" "sky.jpeg")
          coord-pair (ex/get-gps-location file)]
      (is (<= 50.898 (first coord-pair) 50.899))
      (is (<= -0.0711 (second coord-pair) -0.071))))
  (testing "gps-metadata-coords-double-neg"
    (let [file (ps/media-path "_test" "sunset.jpeg")
          coord-pair (ex/get-gps-location file)]
      (is (<= 51.175 (first coord-pair) 51.176))
      (is (<= -4.214 (second coord-pair) -4.213)))))

