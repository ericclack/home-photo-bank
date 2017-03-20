(ns clojure-photo-bank.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[clojure-photo-bank started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-photo-bank has shut down successfully]=-"))
   :middleware identity})
