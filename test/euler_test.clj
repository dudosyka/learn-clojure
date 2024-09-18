(ns euler-test
  (:require [clojure.test :refer [deftest is]]
            [euler.problem-10 :as problem-10]
            [euler.problem-21 :as problem-21]))

(deftest test-problem-10-solve
  (is (= (problem-10/solve {'n 2000000}) 142913828923)))

(deftest test-problem-21-solve
  (is (= (problem-21/solve {'n 10000}) 31626)))

(deftest test-problem-21-solve-easy
  (is (= (problem-21/solve-easy {'n 10000}) 31626)))