(ns clojure-image-bank.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [clojure-image-bank.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[clojure-image-bank started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-image-bank has shut down successfully]=-"))
   :middleware wrap-dev})
