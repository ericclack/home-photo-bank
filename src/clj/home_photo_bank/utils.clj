(ns home-photo-bank.utils
  "Useful functions with few dependencies"
  
  (:require [clojure.string :as s]
            [clojure.pprint :refer [pprint pp]]
            [clojure.tools.logging :as log]
            ))

(defn parent-category [category]
  (s/join "/" (drop-last 1 (s/split category #"/"))))


