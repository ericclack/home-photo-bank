(ns clojure-photo-bank.routes.selection
  (:require [clojure-photo-bank.layout :as layout]
            [compojure.core :refer [defroutes context GET ANY POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [file-response]]
            [clojure.java.io :as io]
            [clojure-photo-bank.photo-store :as ps]
            [clojure-photo-bank.models.db :as db]
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

;; ----------------------------------------------------

(defroutes selection-routes
  (context "/photos/_select" []
           (GET "/" [] (selection-page))
           (POST "/:photo-path{.*}" [photo-path] (toggle-select-photo photo-path))))
