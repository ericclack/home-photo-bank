(ns clojure-image-bank.test.image
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [clojure-image-bank.handler :refer :all]
            [clojure.java.io    :as io]
            [image-resizer.core :refer :all]
            [image-resizer.format :as format]))

(deftest test-image
  (testing "resize"
    (let [image-path "resources/public/img/" 
          file (io/file (str image-path "boat.jpg"))
          thumbnail (resize file 100 100)
          outfilepath (format/as-file thumbnail (str image-path "out.jpg"))]
      (is (= (str image-path "out_50x33.jpg") outfilepath)))))

