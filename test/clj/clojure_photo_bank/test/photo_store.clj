(ns clojure-photo-bank.test.photo-store
  (:require [clojure.test :refer :all]
            [clojure-photo-bank.photo-store :as ps]
            [clojure.string :as s]))

;; These tests are hard-coded to my data, that needs to be
;; changed!

(deftest test-1
  (testing "get years"
    (let [years (ps/top-level-categories)]
      (is (= "2017" (first years))))))
