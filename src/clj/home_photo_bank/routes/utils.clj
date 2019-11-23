(ns home-photo-bank.routes.utils
  (:require [home-photo-bank.layout :as layout]
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

