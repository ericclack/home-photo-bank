(ns home-photo-bank.core
  (:require [home-photo-bank.handler :as handler]
            [luminus.repl-server :as repl]
            [luminus.http-server :as http]
            [home-photo-bank.config :refer [env]]
            [home-photo-bank.photo-store :as ps]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [mount.core :as mount])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(mount/defstate ^{:on-reload :noop}
                http-server
                :start
                (http/start
                  (-> env
                      (assoc :handler (handler/app))
                      (update :port #(or (-> env :options :port) %))))
                :stop
                (http/stop http-server))

(mount/defstate ^{:on-reload :noop}
                repl-server
                :start
                (when-let [nrepl-port (env :nrepl-port)]
                  (let [nrepl-bind (env :nrepl-bind)
                        args (merge {:port nrepl-port}
                                    (when nrepl-bind {:bind nrepl-bind}))]
                  (repl/start args)))
                :stop
                (when repl-server
                  (repl/stop repl-server)))


(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn start-importer []
  (let [importer (future (ps/watch-and-import!))]
    (log/info "started importer - " importer)
    importer))

(defn -main [& args]
  (start-app args)
  (start-importer))
