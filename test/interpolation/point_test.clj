(ns interpolation.point-test
  (:require [clojure.test :refer [deftest testing is]]
            [interpolation.point :refer [parse-from-line to-string]])
  (:import [interpolation.point Point]))

(deftest test-point-deserialization
  (testing "Test point deserialization (string to record)"
    (is (= (Point. 1.0 2.0) (parse-from-line "1;2")) "Test correct string")
    (is (thrown? Exception "Bad line provided" (parse-from-line "1:2")) "Test incorrect string")))

(deftest test-point-serialization
  (testing "Test point serialization (record to string)"
    (is (= "(1.0;2.0)" (to-string (Point. 1.0 2.0))))))