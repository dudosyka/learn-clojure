(ns rb-tree-test
  (:require [clojure.test :refer [deftest is testing]])
  (:refer-clojure :exclude [merge map filter keys into get conj abs])
  (:require [rb-tree.core :refer [add conj delete filter get into keys map rb-dict reduce-left reduce-right values]]))

(deftest test-rb-creation
  (testing "Test dict creation"
    (is (thrown? Exception (rb-dict 1)) "Test for invalid arguments number")
    (is (= (rb-dict :a 1 :b 2 :c 3) {:a 1 :b 2 :c 3})) "Test success creation"
    (is (thrown? Exception (rb-dict 1 2 "c" 3)) "Test incomparable keys")
    (is (= (rb-dict :a "c" :b 2) {:a "c" :b 2}) "Test different values types")
    (is (= (rb-dict :a "c" :b 2) (rb-dict :b 2 :a "c")) "Test commutative")))

(deftest test-rb-add-element
  (testing "Test new elements insertion"
    (is (= (-> (rb-dict) (add 1 2) (add 3 4)) {1 2 3 4})) "Test success insertion"
    (is (thrown? Exception (-> (rb-dict) (add 1 2) (add "string-key" 3))) "Test incomparable keys")
    (is (= (-> (rb-dict) (add 1 2) (add 2 3)) (-> (rb-dict) (add 2 3) (add 1 2))) "Test commutative")
    (is (= (-> (rb-dict) (add 3 (-> (rb-dict) (add 1 2)))) {3 {1 2}}) "Test nested dict insertion")))

(deftest test-rb-remove-element
  (testing "Test removing elements"
    (is (= (-> (rb-dict 1 2 2 3 3 4) (delete 1)) (rb-dict 2 3 3 4)))
    (is (= (-> (rb-dict 1 2) (delete 2)) (-> (rb-dict 1 2))))
    (is (= (-> (rb-dict 1 2) (delete 1)) (rb-dict)))))

(deftest test-rb-get-element
  (testing "Test get elements"
    (is (= ((rb-dict 1 2 3 4) 1) 2))
    (is (= (get (rb-dict :k 2 :k2 4) :k) 2))
    (is (= ((rb-dict) 1) nil))
    (is (= ((rb-dict 1 2) 2) nil))))

(deftest test-rb-conjunction
  (testing "Test dicts conjunction"
    (is (= (conj (rb-dict 1 2) (rb-dict 2 3)) (rb-dict 1 2 2 3)))
    (is (thrown? Exception (conj (rb-dict 1 2) (rb-dict "2" 3))))
    (is (= (conj (rb-dict 3 (rb-dict 1 2)) (rb-dict 3 (rb-dict 3 4)))
           (rb-dict 3 (rb-dict 1 2 3 4))))
    (is (= (conj (rb-dict 3 (rb-dict 1 2)) (rb-dict 3 (rb-dict 1 2)))
           (rb-dict 3 (rb-dict 1 4))))))

(deftest test-rb-reducing
  (testing "Test reducing elements of dict"
    (is (= "abcd"
           (reduce-left (rb-dict :a "a" :b "b" :c "c" :d "d") "" (fn [acc [_ v]] (str acc v)))))
    (is (= "dcba"
           (reduce-right (rb-dict :a "a" :b "b" :c "c" :d "d") "" (fn [acc [_ v]] (str acc v)))))))

(deftest test-rb-mapping
  (testing "Test mapping through elements of dict"
    (is (= '(4 6 8)
           (map (rb-dict 1 2 2 3 3 4) (fn [[_ v]] (* v 2)))))
    (is (= [[2 4] [4 6] [6 8]]
           (map (rb-dict 1 2 2 3 3 4) (fn [[k v]] [(* k 2) (* v 2)]))))
    (is (= (rb-dict 2 4 4 6 6 8)
           (into (rb-dict) (map (rb-dict 1 2 2 3 3 4) (fn [[k v]] [(* k 2) (* v 2)])))))
    (is (= []
           (map (rb-dict) (constantly nil))))))

(deftest test-rb-filtering
  (testing "Test filtering elements of dict"
    (is (= [[2 4]]
           (filter (rb-dict 1 2 3 4 2 4) #(and (even? (first %)) (even? (second %))))))
    (is (= []
           (filter (rb-dict) (constantly true))))
    (is (= []
           (filter (rb-dict 1 2) #(even? (first %)))))
    (is (= []
           (filter (rb-dict 2 3) (constantly false))))))

(deftest test-values-getter
  (testing "Test dict values getter"
    (is (= []
           (values (rb-dict))))
    (is (= [1 2 3]
           (values (rb-dict 1 1 2 2 3 3))))))

(deftest test-keys-getter
  (testing "Test dict keys getter"
    (is (= []
           (keys (rb-dict))))
    (is (= [1 2 3]
           (keys (rb-dict 1 1 2 2 3 3))))))
