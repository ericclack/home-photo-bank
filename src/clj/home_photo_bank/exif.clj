(ns home-photo-bank.exif
  "Exif metadata from our images"
  
  (:require [home-photo-bank.constants :as const]
            [clojure.string :as s]
            [clj-time.format :as tf]            
            [exif-processor.core :as exifp]
            [clojure.edn :as edn]

            [home-photo-bank.shell :as shell]
            [home-photo-bank.utils :as u]
            ))

(defn get-metadata
  [file]
  (exifp/exif-for-file file))

(defn has-date-created? [metadata]
  (let [date-created (get metadata "Date/Time Original")]
    (and (some? date-created)
         (not= date-created const/exif-null-date))))

(defn get-date-created
  "EXIF time is in format: 2003:12:14 12:01:44"
  [metadata]
  (tf/parse const/exif-formatter
            (get metadata "Date/Time Original")))

(defn get-exif-date-created [file]
  (get-date-created (get-metadata file)))

(defn set-exif-date-created!
  "Set date-created with a string in
  format 2006-06-01T10:11"
  [file date-created]
  (shell/set-exif-date-created! file date-created))

(defn get-artist
  [metadata]
  (get metadata "Artist"))

(defn get-gps-location-dms [file]
  (let [metadata (get-metadata file)
        lats (metadata "GPS Latitude")
        longs (metadata "GPS Longitude")
        lat-ref (metadata "GPS Latitude Ref")
        long-ref (metadata "GPS Longitude Ref")]
    (if (and lats longs)
      (let [del-chars #"[°'\"]"
            lat-list (map edn/read-string (s/split (s/replace lats del-chars "") #" "))
            long-list (map edn/read-string (s/split (s/replace longs del-chars "") #" "))]
        (list (concat lat-list (list lat-ref))
              (concat long-list (list long-ref)))))))

(defn get-gps-location [file]
  (let [dms-pair (get-gps-location-dms file)]
    (if (some? dms-pair)
      (list (apply u/dms->coord (first dms-pair))
            (apply u/dms->coord (second dms-pair))))))
