(ns euler.problem-10
  (:require [core :as core]))

(defn get-n-size-map [n]
  (loop [i 2 arr {}]
    (if (<= i n)
      (recur (+ 1 i) (into arr {i true}))
      arr)))

(defn remove-divides [n map]
  (loop [i (* n n) new-map map size (+ 1 (count map))]
    (if (<= i size)
      (recur (+ i n) (assoc new-map i false) size)
      new-map)))

(defn sum-primes
  ([n] (sum-primes n 2 (get-n-size-map n)))
  ([n cur values]
   (if (<= cur (Math/ceil (Math/sqrt n)))
     (recur n (+ cur 1) (if (get values cur)
                          (remove-divides cur values)
                          values))
     (reduce + (map first (filter second values))))))

(defn solve [opts]
  (core/compute-with-timer #(sum-primes ('n opts))))

(solve {'n 100})