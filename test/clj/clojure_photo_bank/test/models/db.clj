(ns clojure-photo-bank.test.models.db
  (:require [clojure.test :refer :all]
            [clojure-photo-bank.models.db :as db]
            [clojure.string :as s]))

(deftest test-1
  (testing "can access db"
    (is (some? (db/all-photos)))))


(deftest test-following-item
  (testing "simple"
    (is (= 3 (db/following-item (list 1 2 3 4 5) #(= 2 %))))
    (is (nil? (db/following-item (list 1 2 3 4 5) #(= 5 %)))))
  (testing "following item"
    (let [a-list (list {:name "Fred" :age 10}
                       {:name "Bill" :age 11}
                       {:name "James" :age 12}
                       {:name "Jane" :age 13})
          item1-name "Bill"
          item2 (db/following-item a-list #(= item1-name (:name %)))]
      (is (= "James" (:name item2))))))
                       
