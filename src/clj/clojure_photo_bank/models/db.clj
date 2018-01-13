(ns clojure-photo-bank.models.db
  (:require [environ.core :refer [env]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as q]
            [clojure.string :as s]
            [clojure.set :as set]
            [clojure.core.memoize :as memo]
            [clojure.pprint :refer [pprint pp]]))

(defn db []
  (:db (mg/connect-via-uri (env :database-url))))

(def coll "photos")

(defmacro with-db
  [& body]
  `(q/with-collection (db) coll
     ~@body))

;  `(mg/connect-via-uri (env :database-url)
;     ~@body))

(defn all-photos
  []
  (with-db (q/find {})))

(defn photo-metadata [photo-path]
  (with-db (mc/find-one photo-path)))

;; -------------------------------------------------

(defn photos-with-keyword [word]
  (map #(:doc %)
       (with-db (mc/find "photos" "by_keyword"
                                {:key word
                                 :reduce false
                                 :include_docs true}))))

(defn photos-with-keyword-starting [stem]
  (map #(:doc %)
       (with-db (mc/find "photos" "by_keyword"
                                {:startkey stem
                                 :endkey (str stem "\uffff")
                                 :reduce false
                                 :include_docs true}))))

(defn photos-with-keywords-starting [stems]
  (apply set/intersection
         (map set
              (map photos-with-keyword-starting stems))))

(def all-photo-keywords
  ;; "Return a list of (key, count) pairs"
  (memo/memo
   (fn []
     (map #(list (:key %) (:value %))
          (with-db (mc/find "photos" "by_keyword"
                                   {:reduce true
                                    :group true }))))))

(defn popular-photo-keywords
  "Return the top scoring keywords"
  [n]
  (sort-by first (take n (reverse (sort-by second (all-photo-keywords))))))

(defn photos-in-category [category]
  (map #(:value %)
       (with-db (mc/find "photos" "by_category"
                                {:key category
                                 :reduce false}))))

(defn photos-in-parent-category [category]
  (map #(:value %)
       (with-db (mc/find "photos" "by_parent_category"
                                {:key category
                                 :reduce false}))))

(defn grouped-photos-in-parent-category [category]
  (group-by #(:category %)
            (photos-in-parent-category category)))


(defn following-item
  "Return the item after the item that satisfies pred"
  [a-list pred]
  (cond
    (nil? (second a-list)) nil
    (pred (first a-list)) (second a-list)
    :else (recur (rest a-list) pred)))

(defn previous-item
  "Return the item before the item that satisfies pred"
  [a-list pred]
  (cond
    (nil? (second a-list)) nil
    (pred (second a-list)) (first a-list)
    :else (recur (rest a-list) pred)))
  
(defn next-photo-in-category [category photo-path]
  (let [photos (photos-in-category category)]
    (following-item
     photos
     #(= photo-path (:path %)))))

(defn prev-photo-in-category [category photo-path]
  (let [photos (photos-in-category category)]
    (previous-item
     photos
     #(= photo-path (:path %)))))

;; ----------------------------------------------------------

(defn photos-selected [selection]
  (map #(:doc %)
       (with-db (mc/find "photos" "by_selection"
                                {:key selection
                                 :include_docs true}))))

;; ----------------------------------------------------------

(defn set-photo-metadata!
  "metadata is a complete document"
  [metadata]
  (let [doc (mc/insert (db) coll metadata)]
    (memo/memo-clear! all-photo-keywords)
    doc))

(defn set-photo-keywords! [photo-path keywords]
  (with-db (set-photo-metadata!
            (assoc (mc/find-one photo-path)
                   :keywords (map s/lower-case keywords)))))

(defn set-photo-selection! [photo-path selections]
  (with-db (set-photo-metadata!
            (assoc (mc/find-one photo-path)
                   :selections (map s/lower-case selections)))))
  
