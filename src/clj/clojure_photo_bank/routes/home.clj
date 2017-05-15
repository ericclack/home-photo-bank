(ns clojure-photo-bank.routes.home
  (:require [clojure-photo-bank.layout :as layout]
            [compojure.core :refer [defroutes GET ANY POST]]
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
  (let [all-keywords (db/all-photo-keywords)
        pop-keywords (db/popular-photo-keywords 50)
        random-keyword (first (rand-nth all-keywords))
        keyword-photos (db/photos-with-keyword random-keyword)]
    (render
     "home.html" {:top-level-categories (ps/top-level-categories)
                  :all-keywords all-keywords
                  :pop-keywords pop-keywords
                  :random-keyword random-keyword
                  :keyword-photos keyword-photos})))

(defn category-page
  ([year req] (category-page year nil nil req))
  ([year month req] (category-page year month nil req))
  ([year month day req] 
   (let [category (ps/date-parts-to-category (list year month day))
         photos (if day (db/photos-in-category category) '())
         month-photos (if (not day) (db/grouped-photos-in-parent-category category) '())
         iyear (Integer/parseInt year)
         imonth (when month (Integer/parseInt month)) ]
     (render
      "category.html"
      {:top-level-categories (ps/top-level-categories)
       :year year
       :month month
       :day day
       :category category
       :category-name (ps/category-name category)
       :categories-and-names (ps/categories-and-names category)
       :photos photos
       :month-photos month-photos
       :next-month-category (when imonth (ps/next-month-category iyear imonth))
       :prev-month-category (when imonth (ps/prev-month-category iyear imonth))
       }
      req))))

(defn serve-file [file-path]
  (file-response (str (ps/media-path file-path))))

(defn about-page []
  (render "about.html"))

(defn photo-search [word req]
  (let [trimmed-word (s/trim (s/lower-case word))
        words (s/split trimmed-word #" ")
        photos
        ;; At least two words? Then search for both
        ;; separate words and combined phrase
        (if (second words)
          (concat 
           (db/photos-with-keyword-starting trimmed-word)
           (db/photos-with-keywords-starting words))
          (db/photos-with-keyword-starting trimmed-word))]
  
        (render
         "search.html"
         {:word trimmed-word
          :photos photos
          }
         req)))

(defn str->keywords
  [s]
  (map s/trim (s/split-lines s)))

(defn edit-photo [photo-path keywords back]
  (when keywords
    (db/set-photo-keywords! photo-path (str->keywords keywords)))
  (render
   "edit.html"
   {:photo (db/photo-metadata photo-path)
    :keywords keywords
    :back back
    }))

(defn process-photos
  "Display the next photo for processing"
  ([]
   (let [next (first (ps/process-photos-with-no-keywords))
         name (when next (.getName next))]
     (process-photos name)))
  
  ([photo-path]
   (let [all-photos (ps/photos-to-process)
         num-photos (count all-photos)
         done? (nil? photo-path)
         photo (when-not done? (first (filter #(= photo-path
                                                  (.getName %))
                                              all-photos)))
         name (when-not done? (.getName photo))
         keywords (when-not done? (ps/file-name-to-keywords
                                   (first (ps/split-extension photo))))]
     (render
      "process.html"
      {:num-photos num-photos
       :photo photo
       :name name
       :keywords keywords
       :all-photos all-photos
       :all-photos-names (map #(.getName %) all-photos)}))))

(defn process-photo!
  "Add keywords to this photo"
  [photo-path keywords]
   (ps/process-photo-add-keywords!
    (io/file (ps/media-path "_process" photo-path))
    (str->keywords keywords))
   (process-photos))

(defn processing-done!
  []
  (log/info
   (ps/move-processed-to-import!))
  (process-photos))

;; ----------------------------------------------------

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/photos/_search" [word :as req] (photo-search word req))

  (GET "/photos/_process" [] (process-photos))
  (GET "/photos/_process/:photo-path{.*}" [photo-path]
        (process-photos photo-path))
  
  (POST "/photos/_process/:photo-path{.*}" [photo-path keywords]
        (process-photo! photo-path keywords))
  (POST "/photos/_processing-done" [] (processing-done!))

  (ANY "/photos/_edit/:photo-path{.*}" [photo-path keywords back]
       (edit-photo photo-path keywords back))
  
  (GET "/photos/:year" [year :as req] (category-page year req))
  (GET "/photos/:year/:month" [year month :as req]
       (category-page year month req))
  (GET "/photos/:year/:month/:day" [year month day :as req]
       (category-page year month day req))

  (GET "/media/:file-path{.*}" [file-path] (serve-file file-path))
  
  (GET "/about" [] (about-page)))

