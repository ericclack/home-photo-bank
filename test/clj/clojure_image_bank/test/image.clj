(ns clojure-image-bank.test.image
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [clojure-image-bank.handler :refer :all]
            [clojure.java.io    :as io]
            [environ.core :refer [env]]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]))

(defn media-path [file]
  (str (env :media-path) "/" file))

(deftest test-image
  (testing "resize"
    (let [file (io/file (media-path "2017/2/boat.jpg"))
          thumbnail (resize file 100 100)
          outfilepath (format/as-file thumbnail (media-path "out.jpg"))]
      (is (= (media-path "out_50x33.jpg") outfilepath)))))

