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

(defn render
  "render this template adding in request and back
  link if not already included in the data"
  ([template] (render template {} nil))
  ([template data] (render template data nil))
  ([template data request]
   (let [back (or (:back data)
                  (when (some? request)
                    (str (:uri request) "?" (:query-string request))))]
     (layout/render template
                    (assoc data
                           :request request
                           :back back)))))

(defn home-page []
  (let [keywords (db/all-photo-keywords)
        random-keyword (first (rand-nth keywords))
        keyword-photos (db/photos-with-keyword random-keyword)]
    (render
     "home.html" {:top-level-categories (ps/top-level-categories)
                  :all-keywords keywords 
                  :random-keyword random-keyword
                  :keyword-photos keyword-photos})))

(defn category-page
  ([year req] (category-page year nil nil req))
  ([year month req] (category-page year month nil req))
  ([year month day req] 
   (let [category (ps/date-parts-to-category (list year month day))
         photos (if day (db/photos-in-category category) '())
         month-photos (if (not day) (db/grouped-photos-in-parent-category category) '())]
     (render
      "category.html"
      {:top-level-categories (ps/top-level-categories)
       :category category
       :category-name (ps/category-name category)
       :categories-and-names (ps/categories-and-names category)
       :photos photos
       :month-photos month-photos}
      req))))

(defn serve-file [file-path]
  (file-response (str (ps/media-path file-path))))

(defn about-page []
  (render "about.html"))

(defn photo-search [word req]
  (let [words (s/split (s/trim word) #" ")]
    (render
     "search.html"
     {:word word
      :photos (db/photos-with-keywords-starting words)
      }
     req)))

(defn edit-photo [photo-path keywords back]
  (when keywords
    (db/set-photo-keywords! photo-path
                            (map s/trim (s/split-lines keywords))))
  (render
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
  
  (GET "/photos/:year" [year :as req] (category-page year req))
  (GET "/photos/:year/:month" [year month :as req]
       (category-page year month req))
  (GET "/photos/:year/:month/:day" [year month day :as req]
       (category-page year month day req))

  (GET "/media/:file-path{.*}" [file-path] (serve-file file-path))
  
  (GET "/about" [] (about-page)))

