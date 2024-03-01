(ns home-photo-bank.routes.process
  (:require [home-photo-bank.layout :as layout]
            [compojure.core :refer [defroutes context GET ANY POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [file-response content-type header]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.string :as s]
            [clj-time.format :as tf]

            [home-photo-bank.photo-store :as ps]
            [home-photo-bank.models.db :as db]
            [home-photo-bank.utils :as u]
            [home-photo-bank.exif :as ex]            
            [home-photo-bank.routes.utils :refer [render]]
            ))

(defn process-photos
  "Display the next photo for processing, or the All Done message"
  ([]
   (let [next (first (ps/process-photos-with-no-keywords))
         name (when next (.getName next))]
     (process-photos name)))
  
  ([photo-path]
   (let [all-photos (ps/photos-to-process)
         num-photos (count all-photos)
         done? (nil? photo-path)
         photo (when-not done? (first (filter #(= photo-path
                                                  (.getName %))
                                              all-photos)))
         name (when-not done? (.getName photo))
         keywords (when-not done? (ps/file-name-to-keywords
                                   (first (ps/split-extension photo))))
         exif (when-not done? (ex/get-metadata photo))
         date-created (when (ex/has-date-created? exif)
                        (ex/get-date-created exif))
         duplicates (when-not done? (ps/maybe-duplicate? photo))]
     (render
      "process.html"
      {:num-photos num-photos
       :photo photo
       :name name
       :keywords keywords
       :date-created date-created
       :duplicates duplicates
       :all-photos all-photos
       :all-photos-names (map #(.getName %) all-photos)}))))

(defn process-delete-photo!
  "Remove this photo from import process by moving it to failed"
  [photo-path]
  (let [file (io/file (ps/media-path "_process" photo-path))]
    (log/info
     (ps/move-photo-into-failed! file))
    (process-photos)))

(defn process-photo!
  "Add keywords to this photo and optionally set
  creation date (format 2006-06-01T10:11 from HTML)"
  [photo-path keywords date-created]
  
  (let [file (io/file (ps/media-path "_process" photo-path))]
    (when date-created
      (ex/set-exif-date-created! file (tf/parse date-created)))
    
    (ps/process-photo-add-keywords! file (u/str->keywords keywords))
    (process-photos)))

(defn processing-done!
  []
  (log/info
   (ps/move-processed-to-import!))
  (process-photos))


;; ----------------------------------------------------

(defroutes process-routes
  (context "/photos/_process" []
           (GET "/" [] (process-photos))
           (GET "/:photo-path{.*}" [photo-path]
                (process-photos photo-path))
  
           (POST "/_done" [] (processing-done!))
           (POST "/_delete/:photo-path{.*}" [photo-path]
                 (process-delete-photo! photo-path))
           (POST "/:photo-path{.*}" [photo-path keywords date-created]
                 (process-photo! photo-path keywords date-created))
           ))
