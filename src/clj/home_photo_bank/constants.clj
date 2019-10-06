(ns home-photo-bank.constants
  "Useful constants for our modules, that never change
  between environment"

  (:require  [clj-time.format :as tf]))

(def exif-date-format "yyyy:MM:dd HH:mm:ss")
(def exif-null-date "0000:00:00 00:00:00")
(def exif-formatter (tf/formatter exif-date-format))
