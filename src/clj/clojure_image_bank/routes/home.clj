(ns clojure-image-bank.routes.home
  (:require [clojure-image-bank.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure-image-bank.image-store :as imgs]))

(defn home-page []
  (layout/render
    "home.html" {:top-level-categories (imgs/top-level-categories)}))

(defn category-page
  ([parent category] (category-page (str parent "/" category)))
  ([category]
   (layout/render
    "category.html"
    {:top-level-categories (imgs/top-level-categories)
     :category category
     :categories (imgs/categories category)})))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/photos/:parent/:category" [parent category] (category-page parent category))
  (GET "/photos/:category" [category] (category-page category))
  (GET "/about" [] (about-page)))

