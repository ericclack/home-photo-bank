(ns clojure-image-bank.test.image
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [clojure-image-bank.handler :refer :all]
            [clojure.java.io    :as io]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]))

(defn media-path
  [& path]
   (.getPath (apply io/file (cons (env :media-path) path))))

(deftest test-image
  (testing "resize"
    (let [file (io/file (media-path "2017" "2" "boat.jpg"))
          thumbnail (resize file 100 100)
          outfilepath (format/as-file thumbnail (media-path "out.jpg"))]
      (is (s/ends-with? outfilepath (media-path "out_100x100.jpg"))))))

