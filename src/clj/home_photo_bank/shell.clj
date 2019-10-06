(ns home-photo-bank.shell
  "Functions that call shell scripts such as exiftool"
  
  (:require [clojure.java.shell :refer [sh]]
            [clojure.tools.logging :as log]
            [clj-time.format :as tf]
            [home-photo-bank.constants :as const]
            ))

(defn set-exif-date-created! [file date-created]
  (log/info "set-exif-date-created!")
  (log/info (sh "exiftool"
                (str "-DateTimeOriginal=\""
                     (tf/unparse const/exif-formatter date-created)
                     "\"")
                (str file))))
