(ns clojure-image-bank.test.image-store
  (:require [clojure.test :refer :all]
            [clojure-image-bank.image-store :as imgs]
            [clojure.string :as s]))

;; These tests are hard-coded to my data, that needs to be
;; changed!

(deftest test-1
  (testing "get years"
    (let [years (imgs/top-level-categories)]
      (is (= "2017" (first years))))))
