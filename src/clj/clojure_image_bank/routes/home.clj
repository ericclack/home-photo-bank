(ns clojure-image-bank.routes.home
  (:require [clojure-image-bank.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [file-response]]
            [clojure.java.io :as io]
            [clojure-image-bank.image-store :as imgs]))

(defn home-page []
  (layout/render
    "home.html" {:top-level-categories (imgs/top-level-categories)}))

(defn category-page
  ([year month] (category-page (str year "/" month)))
  ([year month day] (category-page (str year "/" month "/" day)))
  ([category]
   (layout/render
    "category.html"
    {:top-level-categories (imgs/top-level-categories)
     :category category
     :categories (imgs/categories category)
     :photos (imgs/photos category)})))

(defn serve-file [file-path]
  (file-response (imgs/media-path-string file-path)))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))

  (GET "/photos/:year/:month" [year month] (category-page year month))
  (GET "/photos/:year/:month/:day"
       [year month day] (category-page year month day))
  (GET "/photos/:year" [year] (category-page year))

  (GET "/media/:file-path{.*}" [file-path] (serve-file file-path))
  
  (GET "/about" [] (about-page)))

