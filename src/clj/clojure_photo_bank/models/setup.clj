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
"function(doc) {
  doc.keywords.forEach( function(word) {
    emit(word) });
}"
                                 :reduce
"function(key, values, rereduce) {
  return sum(values);
}"}}))))

(defn setup-db
  "Create all the views we need."
  []
  (create-photo-views))

