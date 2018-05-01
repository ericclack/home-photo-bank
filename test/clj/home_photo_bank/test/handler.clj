(ns home-photo-bank.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [home-photo-bank.handler :refer :all]
            [clojure.string :as s]
            [clojure.tools.logging :as log]))

(deftest test-app0
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))))

(deftest test-home
  (testing "home page contains folders"
    (let [response ((app) (request :get "/"))]
      (is (s/includes? response "2017"))))

  (testing "browse inside 2017"
    (let [response ((app) (request :get "/photos/2017"))]
      (log/info response)
      (is (s/includes? response "January")))))
          
