(ns rb-tree-prop-test
  (:refer-clojure :exclude [into conj])
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check :as check]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [rb-tree.core :refer [conj dict into rb-dict]]
            [rb-tree.vertex :refer [black? red-child child-count]]))

(def gen-dict
  (gen/fmap (fn [list] (into (rb-dict) (partition 2 list))) (gen/list gen/small-integer)))

(deftest test-neutral-element
  (testing "Test operations with neutral element"
    (is (check/quick-check
         100
         (prop/for-all [dict gen-dict]
                       (= (conj dict (rb-dict)) dict))))
    (is (check/quick-check
         100
         (prop/for-all [dict gen-dict]
                       (= (conj (rb-dict) dict) dict))))))

(deftest test-commutative
  (testing "Test that operations is commutative"
    (is (check/quick-check
         100
         (prop/for-all [dict gen-dict
                        second-dict gen-dict]
                       (= (conj dict second-dict) (conj second-dict dict)))))))

(defn check-valid-node [node]
  (if (black? node)
    true
    (and
     (= (red-child node) nil)
     (or
      (= (child-count node) 2)
      (= (child-count node) 0)))))

(defn check-no-red-vertex-with-red-sons [tree]
  (and
   (check-no-red-vertex-with-red-sons (:left dict))
   (check-valid-node tree)
   (check-no-red-vertex-with-red-sons (:right dict))))

(deftest test-no-red-vertex-with-red-sons
  (testing "Test that every red vertex have two black or no sons"
    (is (check/quick-check
         100
         (prop/for-all [dict gen-dict]
                       (= true (check-no-red-vertex-with-red-sons dict)))))))