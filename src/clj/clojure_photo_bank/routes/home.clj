(ns clojure-photo-bank.routes.home
  (:require [clojure-photo-bank.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [file-response]]
            [clojure.java.io :as io]
            [clojure-photo-bank.photo-store :as ps]))

(defn home-page []
  (layout/render
    "home.html" {:top-level-categories (ps/top-level-categories)}))

(defn category-page
  ([year month] (category-page (str year "/" month)))
  ([year month day] (category-page (str year "/" month "/" day)))
  ([category]
   (layout/render
    "category.html"
    {:top-level-categories (ps/top-level-categories)
     :category category
     :category-name (ps/category-name category)
     :categories (ps/categories category)
     :categories-and-names (ps/categories-and-names category)
     :photos (ps/photos-in-category category)})))

(defn serve-file [file-path]
  (file-response (str (ps/media-path file-path))))

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

