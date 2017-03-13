(ns clojure-image-bank.routes.home
  (:require [clojure-image-bank.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure-image-bank.image-store :as imgs]))

(defn home-page []
  (layout/render
    "home.html" {:top-level-categories (imgs/top-level-categories)}))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page)))

