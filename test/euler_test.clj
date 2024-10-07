(ns euler-test
  (:require [clojure.test :refer [deftest is]]
            [euler.problem-10.loop-solve :as problem-10-loop]
            [euler.problem-10.lazy-sequence-solve :as problem-10-lazy]
            [euler.problem-21.modular-solve :as problem-21-modular]
            [euler.problem-21.tail-recursion-solve :as problem-21-tail-recursion]))

(deftest test-problem-10-loop-solve
  (is (= (problem-10-loop/solve 100) 1060)))

(deftest test-problem-10-lazy-solve
  (is (= (problem-10-lazy/solve 100) 1060)))

(deftest test-problem-21-modular-solve
  (is (= (problem-21-modular/solve 1000) 504)))

(deftest test-problem-21-tail-recursion-solve
  (is (= (problem-21-tail-recursion/solve 1000) 504)))