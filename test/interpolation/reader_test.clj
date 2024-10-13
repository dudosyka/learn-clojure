(ns interpolation.reader-test
  (:refer-clojure :exclude [read])
  (:require [clojure.test :refer [deftest is testing]]
            [interpolation.reader :refer [read]])
  (:import [interpolation.point Point]))

(deftest test-reader
  (testing "Test read point from line"
    (with-in-str "1;2"
      (is (= (Point. 1.0 2.0) (read)) "Test correct input"))
    (with-in-str ""
      (is (nil? (read)) "Test empty input"))))