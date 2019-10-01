(ns home-photo-bank.test.utils
  (:require [clojure.test :refer :all]
            [home-photo-bank.utils :as u]
            [clojure.string :as s]))

(deftest parent-category
  (testing "parent from y/m/d category"
    (is (= "2019/09" (u/parent-category "2019/09/30")))
    (is (= "1972/12" (u/parent-category "1972/12/06")))
    (is (= "1980" (u/parent-category "1980/01")))
    (is (= "" (u/parent-category "1980")))
    ))

(deftest search-words-from-url
  (testing "get search word from URL containing from=..."
    (is (= ["apple" "banana"] (u/search-words-from-photo-url "http://0.0.0.0:3000/photo/_next/media/2012/6/16/spain-icecream-1.jpg?from=/photos/_search?word=apple%20banana")))
    (is (= [] (u/search-words-from-photo-url "")))
  ))


