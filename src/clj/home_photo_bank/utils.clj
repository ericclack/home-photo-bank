(ns home-photo-bank.utils
  "Useful functions with few dependencies"
  
  (:require [clojure.string :as s]
            [clojure.pprint :refer [pprint pp]]
            [clojure.tools.logging :as log]
            [ring.util.codec :refer [form-decode]]
            ))

(defn parent-category [category]
  (s/join "/" (drop-last 1 (s/split category #"/"))))

(defn search-words-from-photo-url [url]
  ;; The URL might contain a few ? since one of the
  ;; params is a from-url
  (try
    (let [pos (s/last-index-of url "?")
          params (subs url (+ 1 pos))
          dict (form-decode params)]
      (s/split (get dict "word") #" "))
    (catch Exception e
      [])))
