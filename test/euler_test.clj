(ns euler-test
  (:require [clojure.test :refer [deftest is]]
            [euler.problem-10.loop-solve :as problem-10-loop]
            [euler.problem-10.lazy-sequence-solve :as problem-10-lazy]
            [euler.problem-21.modular-solve :as problem-21-modular]
            [euler.problem-21.tail-recursion-solve :as problem-21-tail-recursion]))

(deftest test-problem-10-loop-solve
  (is (= (problem-10-loop/solve 2000000) 142913828922)))

(deftest test-problem-10-lazy-solve
  (is (= (problem-10-lazy/solve 2000000) 142913828922)))

(deftest test-problem-21-modular-solve
  (is (= (problem-21-modular/solve 10000) 31626)))

(deftest test-problem-21-tail-recursion-solve
  (is (= (problem-21-tail-recursion/solve 10000) 31626)))