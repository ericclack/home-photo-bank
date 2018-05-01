(ns user
  (:require [mount.core :as mount]
            home-photo-bank.core))

(defn start []
  (mount/start-without #'home-photo-bank.core/http-server
                       #'home-photo-bank.core/repl-server))

(defn stop []
  (mount/stop-except #'home-photo-bank.core/http-server
                     #'home-photo-bank.core/repl-server))

(defn restart []
  (stop)
  (start))


