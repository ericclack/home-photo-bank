(ns clojure-photo-bank.models.db
  (:require [com.ashafa.clutch :as couch]
            [environ.core :refer [env]]
            [clojure.string :as s]))

(defmacro with-db
  [& body]
  `(couch/with-db (env :database-url)
     ~@body))

(defn photo-metadata [photo-path]
  (with-db (couch/get-document photo-path)))

(defn set-photo-metadata! [photo-path metadata]
  (with-db (couch/put-document metadata)))

;; -------------------------------------------------

(defn photos-with-keyword [word]
  (map #(:id %)
       (with-db (couch/get-view "photos" "by_keyword" {:key word}))))
