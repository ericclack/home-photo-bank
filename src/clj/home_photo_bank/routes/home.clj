(ns home-photo-bank.routes.home
  (:require [home-photo-bank.layout :as layout]
            [compojure.core :refer [defroutes GET ANY POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [file-response content-type redirect]]
            [ring.util.io :refer [piped-input-stream]]
            [clojure.java.io :as io]
            [home-photo-bank.photo-store :as ps]
            [home-photo-bank.models.db :as db]
            [clojure.tools.logging :as log]
            [clojure.string :as s]
            [clojure.set :as set]
            [clj-time.core :as t]
            ))

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
        random-keyword (first (rand-nth all-keywords))
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
  [photo-path from find-fn]
  (cond (s/starts-with? from "/photos/_search") (redirect from) ;todo

        :else
        (let [current-photo (db/photo-metadata photo-path)
              category (:category current-photo)
              next-photo (find-fn category photo-path)]
          (if (nil? next-photo)
            (redirect (if (= "" from)
                        (str "/photos/" category)
                        from))
            (redirect (str "/photo/" (:path next-photo) "?back=" from))))))

(defn next-photo-page
  "Show the next photo after the one specified by photo-path"
  [photo-path from]
  (adjacent-photo-page photo-path from db/next-photo-by-category))
  
(defn prev-photo-page
  "Show the next photo after the one specified by photo-path"
  [photo-path from]
  (adjacent-photo-page photo-path from db/prev-photo-by-category))

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
  (GET "/photos/_search" [word year :as req] (photo-search word year req))
  (GET "/photos/_keywords" [] (all-keywords))
       
  (GET "/photos/_process" [] (process-photos))
  (GET "/photos/_process/:photo-path{.*}" [photo-path]
        (process-photos photo-path))
  
  (POST "/photos/_process/:photo-path{.*}" [photo-path keywords]
        (process-photo! photo-path keywords))
  (POST "/photos/_processing-done" [] (processing-done!))

  (ANY "/photos/_edit/:photo-path{.*}" [photo-path keywords back]
       (edit-photo photo-path keywords back))
  
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

