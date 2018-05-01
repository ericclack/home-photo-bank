(ns home-photo-bank.routes.selection
  (:require [home-photo-bank.layout :as layout]
            [compojure.core :refer [defroutes context GET ANY POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [file-response content-type header]]
            [clojure.java.io :as io]
            [home-photo-bank.photo-store :as ps]
            [home-photo-bank.models.db :as db]
            [clojure.tools.logging :as log]
            [clojure.string :as s]))

(defn selection-page []
  (let [photos (db/photos-selected "1")]
    (layout/render "selection.html"
                   {:photos photos})))

(defn select-photo [photo-path]
  (db/set-photo-selection! photo-path '("1"))
  (layout/render-json "true"))

(defn unselect-photo [photo-path]
  (db/set-photo-selection! photo-path '())
  (layout/render-json "false"))

(defn toggle-select-photo [photo-path]
  (let [photo (db/photo-metadata photo-path)
        is-selected (some #{"1"} (:selections photo))]
    (if is-selected
      (unselect-photo photo-path)
      (select-photo photo-path))))

(defn download-selection []
  (let [zip-file "/tmp/photo-bank-selection.zip"]
    (ps/selected-photos-as-zip (io/output-stream zip-file))
    (header
     (content-type (file-response zip-file)
                   "application/zip")
     "Content-Disposition" "attachment; filename=\"selection.zip\"")))

;; ----------------------------------------------------

(defroutes selection-routes
  (context "/photos/_select" []
           (GET "/" [] (selection-page))
           (GET "/_download" [] (download-selection))
           (POST "/:photo-path{.*}" [photo-path] (toggle-select-photo photo-path))))
