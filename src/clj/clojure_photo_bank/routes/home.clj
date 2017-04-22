(ns clojure-photo-bank.routes.home
  (:require [clojure-photo-bank.layout :as layout]
            [compojure.core :refer [defroutes GET ANY]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [file-response]]
            [clojure.java.io :as io]
            [clojure-photo-bank.photo-store :as ps]
            [clojure-photo-bank.models.db :as db]
            [clojure.tools.logging :as log]
            [clojure.string :as s]))

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
  ([year] (category-page year nil nil))
  ([year month] (category-page year month nil))
  ([year month day] 
   (let [category (ps/date-parts-to-category (list year month day))
         photos (if day (db/photos-in-category category) '())
         month-photos (if (not day) (db/grouped-photos-in-parent-category category) '())]
     (layout/render
      "category.html"
      {:top-level-categories (ps/top-level-categories)
       :category category
       :category-name (ps/category-name category)
       :categories-and-names (ps/categories-and-names category)
       :photos photos
       :month-photos month-photos}))))

(defn serve-file [file-path]
  (file-response (str (ps/media-path file-path))))

(defn about-page []
  (layout/render "about.html"))

(defn photo-search [word req]
  (layout/render
   "search.html"
   {:word word
    :photos (db/photos-with-keyword-starting word)
    :back (str (:uri req) "?" (:query-string req))
    }))

(defn edit-photo [photo-path keywords back]
  (when keywords
    (db/set-photo-keywords! photo-path
                            (map s/trim (s/split-lines keywords))))
  (layout/render
   "edit.html"
   {:photo (db/photo-metadata photo-path)
    :keywords keywords
    :back back
    }))

;; ----------------------------------------------------

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/photos/_search" [word :as req] (photo-search word req))
  (ANY "/photos/_edit/:photo-path{.*}" [photo-path keywords back]
       (edit-photo photo-path keywords back))
  
  (GET "/photos/:year/:month" [year month] (category-page year month))
  (GET "/photos/:year/:month/:day"
       [year month day] (category-page year month day))
  (GET "/photos/:year" [year] (category-page year))

  (GET "/media/:file-path{.*}" [file-path] (serve-file file-path))
  
  (GET "/about" [] (about-page)))

