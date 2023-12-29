(ns home-photo-bank.routes.home
  (:require [home-photo-bank.layout :as layout]
            [compojure.core :refer [defroutes GET ANY POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [file-response content-type redirect]]
            [ring.util.io :refer [piped-input-stream]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]            
            [clojure.string :as s]
            [clojure.set :as set]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            
            [home-photo-bank.photo-store :as ps]
            [home-photo-bank.models.db :as db]
            [home-photo-bank.utils :as u]
            [home-photo-bank.routes.utils :refer [render]]
            ))

(defn month-photos
  "A list of lists of photos for this month
  nested by day: (day1 (photos...) day2 (photos...))
  sorted by date."
  [category]
  (let [grouped-photos (db/grouped-photos-in-parent-category category)
        sorted-keys (ps/sort-categories (keys grouped-photos))]
    (map #(list % (get grouped-photos %))
         sorted-keys)))

;; ---------------------------------------------------

(defn home-page []
  (let [all-keywords (db/all-photo-keywords)
        pop-keywords (db/popular-photo-keywords 100)
        random-keyword (if (seq all-keywords)
                         (first (rand-nth all-keywords)))
        keyword-photos (db/photos-with-keyword random-keyword)]
    (render
     "home.html" {:top-level-categories (ps/top-level-categories)
                  :all-keywords all-keywords
                  :pop-keywords pop-keywords
                  :random-keyword random-keyword
                  :keyword-photos keyword-photos})))

(defn photo-page [photo-path back]
  (render
   "photo.html" {:photo (db/photo-metadata photo-path)
                 :back back}))

(defn adjacent-photo-page
  "Given photo identified by photo-path show the next/priv photo
  using the find-fn"
  [photo-path from direction]

  (let [search? (s/starts-with? from "/photos/_search")
        current-photo (db/photo-metadata photo-path)
        category (:category current-photo)
        words (u/search-words-from-photo-url from)

        find-fn
        (cond
          (and search? (= direction 'next)) db/next-photo-by-search 
          (and search? (= direction 'prev)) db/prev-photo-by-search 
          (= direction 'next) db/next-photo-by-category
          (= direction 'prev) db/prev-photo-by-category)

        next-photo (if search?
                     (find-fn words photo-path)
                     (find-fn category photo-path))]
    
    (if (nil? next-photo)
      ;; No more photos, so return to where we came from
      (redirect (if (= "" from)
                  (str "/photos/" category)
                  from))
      ;; Redirect to the next photo
      (redirect (str "/photo/" (:path next-photo) "?back=" from)))))

(defn next-photo-page
  "Show the next photo after the one specified by photo-path"
  [photo-path from]
  (adjacent-photo-page photo-path from 'next))
  
(defn prev-photo-page
  "Show the next photo after the one specified by photo-path"
  [photo-path from]
  (adjacent-photo-page photo-path from 'prev))

(defn category-page
  ([year req] (category-page year nil nil req))
  ([year month req] (category-page year month nil req))
  ([year month day req] 
   (let [category (ps/date-parts-to-category (list year month day))
         photos (if day (db/photos-in-category category) '())
         month-photos (if (not day) (month-photos category) '())
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

(defn year-page
  [year req]
  (let [category (ps/date-parts-to-category (list year nil nil))
        categories-and-names (ps/categories-and-names category)
        category-photos-and-names
        (map #(list
               (db/category-photo (str year "/" (first %)))
               (second %)
               (str year "/" (first %)))
             categories-and-names)]
    (render
     "year.html"
     {:top-level-categories (ps/top-level-categories)
      :year year
      :category category
      :categories-and-names categories-and-names
      :category-photos-and-names category-photos-and-names
      }
     req)))
    
(defn serve-file
  [file-path resize]
  (if (nil? resize)
    (file-response (str (ps/media-path file-path)))
    (let* [size (Integer/parseInt resize)
           stream (ps/resized-file-as-stream
                   (ps/media-path file-path)
                   size)]
      (content-type {:body stream }
                    "image/jpeg"))))

(defn about-page []
  (render "about.html"))

(defn photo-search [word year req]
  (let [trimmed-word (s/trim (s/lower-case word))
        words (s/split trimmed-word #" ")
        iyear (when year (Integer/parseInt year))
        
        ;; All photos that match the keywords
        all-photos (db/photo-search words)

        ;; Photos for year, if specified
        photos (if iyear
                 (db/photos-in-year all-photos iyear)
                 all-photos)
        
        ;; Things for search narrowing
        keywords (set/difference (db/keywords-across-photos all-photos)
                                 (set words))
        years (db/years-across-photos all-photos)]
  
        (render
         "search.html"
         {:word trimmed-word
          :year iyear
          :photos photos
          :keywords-across-photos (sort keywords)
          :years-across-photos (sort years)
          }
         req)))

(defn all-keywords []
  (let [all-keywords (db/all-photo-keywords)]
    (render "keywords.html"
            {:top-level-categories (ps/top-level-categories)
             :all-keywords all-keywords})))

(defn edit-photo [photo-path keywords back notes]
  (when keywords
    (db/set-photo-keywords! photo-path (u/str->keywords keywords)))
  (when notes
    (db/set-photo-notes! photo-path notes))
  (render
   "edit.html"
   {:photo (db/photo-metadata photo-path)
    :keywords keywords
    :back back
    :notes notes
    }))

;; ----------------------------------------------------

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/photos/_search" [word year :as req] (photo-search word year req))
  (GET "/photos/_keywords" [] (all-keywords))

  (ANY "/photos/_edit/:photo-path{.*}" [photo-path keywords back notes]
       (edit-photo photo-path keywords back notes))
  
  (GET "/photos/:year" [year :as req] (year-page year req))
  
  (GET "/photos/:year/:month" [year month :as req]
       (category-page year month req))
  (GET "/photos/:year/:month/:day" [year month day :as req]
       (category-page year month day req))

  (GET "/photo/_next/:photo-path{.*}" [photo-path from] (next-photo-page photo-path from))
  (GET "/photo/_prev/:photo-path{.*}" [photo-path from] (prev-photo-page photo-path from))
  (GET "/photo/:photo-path{.*}" [photo-path back] (photo-page photo-path back))
  (GET "/media/:file-path{.*}" [file-path resize] (serve-file file-path resize))
  
  (GET "/about" [] (about-page)))

