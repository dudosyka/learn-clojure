(ns euler.problem-10.lazy-sequence-solve)

(def primes
  (remove
   (fn [x]
     (some #(zero? (mod x %)) primes))
   (iterate inc 2)))

(defn solve [n]
  (reduce + (take-while #(<= % n) primes)))