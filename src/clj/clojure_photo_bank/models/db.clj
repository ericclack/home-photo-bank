(ns clojure-photo-bank.models.db
  (:require [com.ashafa.clutch :as couch]
            [environ.core :refer [env]]
            [clojure.string :as s]
            [clojure.set :as set]
            [clojure.pprint :refer [pprint pp]]))

(defmacro with-db
  [& body]
  `(couch/with-db (env :database-url)
     ~@body))

(defn photo-metadata [photo-path]
  (with-db (couch/get-document photo-path)))

(defn set-photo-metadata! [photo-path metadata]
  (with-db (couch/put-document metadata)))

(defn set-photo-keywords! [photo-path keywords]
  (with-db (set-photo-metadata! photo-path
                                (assoc (couch/get-document photo-path)
                                       :keywords (map s/lower-case keywords)))))

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

(defn photos-with-keywords-starting [stems]
  (apply set/intersection
         (map set
              (map photos-with-keyword-starting stems))))

(defn all-photo-keywords
  "Return a list of (key, count) pairs"
  []
  (map #(list (:key %) (:value %))
       (with-db (couch/get-view "photos" "by_keyword"
                                {:reduce true
                                 :group true }))))
(defn popular-photo-keywords
  "Return the top scoring keywords"
  [n]
  (sort-by first (take n (reverse (sort-by second (all-photo-keywords))))))

(defn photos-in-category [category]
  (map #(:value %)
       (with-db (couch/get-view "photos" "by_category"
                                {:key category
                                 :reduce false}))))

(defn photos-in-parent-category [category]
  (map #(:value %)
       (with-db (couch/get-view "photos" "by_parent_category"
                                {:key category
                                 :reduce false}))))

(defn grouped-photos-in-parent-category [category]
  (group-by #(:category %)
            (photos-in-parent-category category)))