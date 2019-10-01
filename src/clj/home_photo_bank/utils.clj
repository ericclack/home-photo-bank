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
  (let [pos (s/last-index-of url "?")
        params (if pos (subs url (+ 1 pos)) "")
        dict (form-decode params)
        word (get dict "word")]
    (if word
      (s/split word #" ")
      [])))
