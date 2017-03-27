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
  (map #(:doc %)
       (with-db (couch/get-view "photos" "by_keyword"
                                {:key word
                                 :reduce false
                                 :include_docs true}))))

(defn photos-with-keyword-starting [stem]
  (map #(:doc %)
       (with-db (couch/get-view "photos" "by_keyword"
                                {:startkey stem
                                 :endkey (str stem "\uffff")
                                 :reduce false
                                 :include_docs true}))))

(defn all-photo-keywords []
  "Return a list of (key, count) pairs"
  (map #(list (:key %) (:value %))
       (with-db (couch/get-view "photos" "by_keyword"
                                {:reduce true
                                 :group true }))))

(defn photos-in-category [category]
  (map #(:value %)
       (with-db (couch/get-view "photos" "by_category"
                                {:key category
                                 :reduce false}))))
