(ns clojure-photo-bank.test.photo-store
  (:require [clojure.test :refer :all]
            [clojure-photo-bank.photo-store :as ps]
            [clojure.string :as s]))

;; These tests are hard-coded to my data, that needs to be
;; changed!

(deftest test-1
  (testing "get years"
    (let [years (ps/top-level-categories)]
      (is (re-find #"^\d\d\d\d$" (first years))))))

(deftest category-names
  (testing "month names instead of numbers"
    (is (= "January 2017" (ps/category-name "2017/1")))
    (is (= "1 February 2016" (ps/category-name "2016/2/1")))))

(deftest photo-metadata
  (testing "file name to keywords"
    (is (= (list "apple" "pear" "orange")
           (ps/file-name-to-keywords "apple-pear-orange")))
    (is (= (list "apple pie" "orange")
           (ps/file-name-to-keywords "apple_pie-orange")))))
