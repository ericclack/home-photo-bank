(ns clojure-photo-bank.models.db
  (:require [environ.core :refer [env]]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as q]
            [monger.operators :refer :all]
            [monger.joda-time :refer :all]
            [clojure.string :as s]
            [clojure.set :as set]
            [clojure.core.memoize :as memo]
            [clojure.pprint :refer [pprint pp]]
            [clj-time.core :as t]            
            [clj-time.format :as tf]))

(def db
  (:db (mg/connect-via-uri (env :database-url))))

(def coll "photos")

(defmacro with-db
  [& body]
  `(q/with-collection db coll
     ~@body
     (q/batch-size 5000)))

;  `(mg/connect-via-uri (env :database-url)
;     ~@body))

(defn all-photos
  []
  (with-db (q/find {})))

(defn photo-metadata [photo-path]
  (first (with-db (q/find {:_id photo-path}))))

;; -------------------------------------------------

(defn photos-with-keyword [word]
  (with-db (q/find {:keywords word})
    (q/sort {:datetime 1})))

(defn photos-with-keyword-starting [stem]
  (with-db (q/find {:keywords {$regex (str "^" stem)}})
    (q/sort {:datetime 1})))

(defn photos-with-keywords-starting [stems]
  (apply set/intersection
         (map set
              (map photos-with-keyword-starting stems))))

(def all-photo-keywords
  ;; "Return a list of (key, count) pairs"
  (memo/memo
   (fn []
     (map #(list (:_id %) (:count %))
          (mc/aggregate db coll
                        [{ $project {:keywords 1 :_id 0}}
                         { $unwind "$keywords" }
                         { $group {:_id "$keywords" :count {$sum 1}}}
                         { $sort {:_id 1}} ])))))

(defn popular-photo-keywords
  "Return the top scoring keywords"
  [n]
  (sort-by first (take n (reverse (sort-by second (all-photo-keywords))))))

(defn keywords-across-photos [photos]
  (set (flatten (map :keywords photos))))

(defn years-across-photos [photos]
  (set
   (map #(t/year (:datetime %))
        ;; Not all photos have a date time
        ;; (historical data quality issue)
        (filter :datetime photos))))

(defn photos-in-category [category]
  (with-db (q/find {:category category})
    (q/sort {:datetime 1})))

(defn photos-in-parent-category
  "Return photos in parent category such as 2017/1"
  [category]
  (with-db (q/find {:category {$regex (str "^" category "/")}})
    (q/sort {:datetime 1})))

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
  (with-db (q/find {:selections {$in [selection]}})))

;; ----------------------------------------------------------

(defn set-photo-metadata!
  "metadata is a complete document"
  [metadata]
  (let [doc (mc/save-and-return db coll metadata)]
    (memo/memo-clear! all-photo-keywords)
    doc))

(defn update-photo-metadata!
  [photo-path key value]
  (set-photo-metadata!
   (assoc (photo-metadata photo-path)
          (keyword key) value)))

(defn set-photo-keywords! [photo-path keywords]
  (update-photo-metadata! photo-path "keywords"
                         (map s/lower-case keywords)))

(defn set-photo-selection! [photo-path selections]
  (update-photo-metadata! photo-path "selections"
                          (map s/lower-case selections)))
  
;; ----------------------------------------------------------

(defn photos-without-metadatum
  "Find all photos without this piece of metadata"
  [m]
  (with-db (q/find {(keyword m) {"$exists" false}})))

