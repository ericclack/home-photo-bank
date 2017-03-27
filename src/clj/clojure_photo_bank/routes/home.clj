(ns clojure-photo-bank.routes.home
  (:require [clojure-photo-bank.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [file-response]]
            [clojure.java.io :as io]
            [clojure-photo-bank.photo-store :as ps]
            [clojure-photo-bank.models.db :as db]))

(defn home-page []
  (let [keywords (db/all-photo-keywords)
        random-keyword (first (rand-nth keywords))
        keyword-photos (db/photos-with-keyword random-keyword)]
    (layout/render
     "home.html" {:top-level-categories (ps/top-level-categories)
                  :all-keywords keywords 
                  :random-keyword random-keyword
                  :keyword-photos keyword-photos})))

(defn category-page
  ([year month] (category-page (str year "/" month)))
  ([year month day] (category-page (str year "/" month "/" day)))
  ([category]
   (layout/render
    "category.html"
    {:top-level-categories (ps/top-level-categories)
     :category category
     :category-name (ps/category-name category)
     :categories-and-names (ps/categories-and-names category)
     :photos (db/photos-in-category category)})))

(defn serve-file [file-path]
  (file-response (str (ps/media-path file-path))))

(defn about-page []
  (layout/render "about.html"))

(defn photo-search [word]
  (layout/render
   "search.html"
   {:word word
    :photos (db/photos-with-keyword-starting word)
    }))

;; ----------------------------------------------------

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/photos/_search" [word] (photo-search word))
  
  (GET "/photos/:year/:month" [year month] (category-page year month))
  (GET "/photos/:year/:month/:day"
       [year month day] (category-page year month day))
  (GET "/photos/:year" [year] (category-page year))

  (GET "/media/:file-path{.*}" [file-path] (serve-file file-path))
  
  (GET "/about" [] (about-page)))

