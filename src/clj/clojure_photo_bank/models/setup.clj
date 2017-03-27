(ns ^{:doc "Set up CouchDB for the photo bank. Run these functions 
            once after install with `lein setup-db`"}
    clojure-photo-bank.models.setup
  (:require [com.ashafa.clutch :as couch]
            [clojure-photo-bank.models.db :as db]))

(defn create-photo-views 
  "Create CouchDB views photos/by_keyword - a simple search index."
  []
  (db/with-db
    (couch/save-view "photos" (couch/view-server-fns
                               :javascript
{:by_keyword
 {:map
  "
function(doc) {
  doc.keywords.forEach( function(word) {
    if (word.length > 2) {
      emit(word, 1);
    }});
}"
  :reduce
  "
function(key, values, rereduce) {
  return sum(values);
}"}

 :by_category
 {:map
  "
function(doc) {
  emit(doc.category, doc);
}"}}
))))

(defn setup-db
  "Create all the views we need."
  []
  (create-photo-views))

