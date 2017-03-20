(ns clojure-photo-bank.models.db
  (:require [com.ashafa.clutch :as couch]
            [environ.core :refer [env]]
            [clojure.string :as s]
            [clojure-photo-bank.photo-store :as ps]))

(defmacro with-db
  [& body]
  `(couch/with-db (env :database-url)
     ~@body))

(defn photo-metadata [photo-path]
  (with-db (couch/get-document photo-path)))

(defn set-photo-metadata! [photo-path metadata]
  (with-db (couch/put-document metadata)))

;; -------------------------------------------------

(defn make-photo-metadata [photo]
  {:_id (str photo)
   :path (str photo)
   :filename (.getName photo)
   :name (first (s/split (.getName photo) #"\."))
   :category (s/replace (.getParent photo)
                        (str (env :media-path) "/")
                        "")
   })

