(ns user
  (:require [mount.core :as mount]
            clojure-photo-bank.core))

(defn start []
  (mount/start-without #'clojure-photo-bank.core/http-server
                       #'clojure-photo-bank.core/repl-server))

(defn stop []
  (mount/stop-except #'clojure-photo-bank.core/http-server
                     #'clojure-photo-bank.core/repl-server))

(defn restart []
  (stop)
  (start))


