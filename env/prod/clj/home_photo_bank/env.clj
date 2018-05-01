(ns home-photo-bank.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[home-photo-bank started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[home-photo-bank has shut down successfully]=-"))
   :middleware identity})
