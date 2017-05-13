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

(deftest keywords
  (testing "file name to keywords"
    (is (= (list "apple" "pear" "orange")
           (ps/file-name-to-keywords "apple-pear-orange")))
    (is (= (list "apple pie" "orange")
           (ps/file-name-to-keywords "apple_pie-orange")))
    (is (= (list "apple" "banana")
           (ps/file-name-to-keywords "apple-banana-1")))
    (is (= (list "apple" "banana")
           (ps/file-name-to-keywords "apple-banana-1-a-b")))
    )
  (testing "keywords to file name"
    (is (= "apple-pear-orange"
           (ps/keywords-to-file-name (list "apple" "pear" "orange"))))
    (is (= "apple_pie-pear"
           (ps/keywords-to-file-name (list "apple pie" "pear"))))))


(deftest dates-and-categories
  (testing "year, month, day -> category"
    (is (= "2013/12" (ps/date-parts-to-category '("2013" "12"))))
    (is (= "2013/12" (ps/date-parts-to-category '("2013" "12" nil))))
    (is (= "2013/12/1" (ps/date-parts-to-category '("2013" "12" "1"))))
    (is (= "2013" (ps/date-parts-to-category '("2013" nil))))
    (is (= "2013" (ps/date-parts-to-category '("2013"))))
    ))

(deftest next-category
  (testing "next-month"
    (is (= "2017/2" (ps/next-month-category 2017 1)))
    (is (= "2018/1" (ps/next-month-category 2017 12))))
  (testing "prev-month"
    (is (= "2017/2" (ps/prev-month-category 2017 3)))
    (is (= "2016/12" (ps/prev-month-category 2017 1))))  
  )
