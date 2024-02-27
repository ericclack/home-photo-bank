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
    (is (= [] (u/search-words-from-photo-url "http://0.0.0.0:3000/photo/_next/media/2012/6/16/spain-icecream-1.jpg?from=/photos/_search?words=apple%20banana")))
  ))

(deftest dms->coord
  (testing "deg minutes seconds N/S/E/W tests"
    (is (= (u/dms->coord 0 0 0 "N") 0.0))
    (is (= (u/dms->coord 50 30 0 "E") 50.5))
    (is (= (u/dms->coord 50 15 0 "S") -50.25))
    (is (<= -50.60001 (u/dms->coord 50 30 36 "W") -50.59999)) ; close enough!
    ))
