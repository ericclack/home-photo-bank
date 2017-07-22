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
  (layout/render-json "\"ok\""))

(defn unselect-photo [photo-path]
  (db/set-photo-selection! photo-path '())
  (layout/render-json "\"ok\""))

;; ----------------------------------------------------

(defroutes selection-routes
  (context "/photos/_select" []
           (GET "/" [] (selection-page))
           (POST "/_drop/:photo-path{.*}" [photo-path] (unselect-photo photo-path))
           (POST "/:photo-path{.*}" [photo-path] (select-photo photo-path))))
