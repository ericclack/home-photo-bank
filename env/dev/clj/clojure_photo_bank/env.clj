(ns clojure-photo-bank.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [clojure-photo-bank.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[clojure-photo-bank started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-photo-bank has shut down successfully]=-"))
   :middleware wrap-dev})
