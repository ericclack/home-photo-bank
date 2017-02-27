(ns clojure-image-bank.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[clojure-image-bank started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-image-bank has shut down successfully]=-"))
   :middleware identity})
