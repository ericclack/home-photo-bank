(ns user
  (:require [mount.core :as mount]
            clojure-image-bank.core))

(defn start []
  (mount/start-without #'clojure-image-bank.core/http-server
                       #'clojure-image-bank.core/repl-server))

(defn stop []
  (mount/stop-except #'clojure-image-bank.core/http-server
                     #'clojure-image-bank.core/repl-server))

(defn restart []
  (stop)
  (start))


