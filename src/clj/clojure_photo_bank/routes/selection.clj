(ns clojure-photo-bank.routes.selection
  (:require [clojure-photo-bank.layout :as layout]
            [compojure.core :refer [defroutes GET ANY POST]]
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

;; ----------------------------------------------------

(defroutes selection-routes
  (GET "/photos/_selection" [] (selection-page)))
