(ns euler.problem-21
  (:require [euler.problem-10 :as problem-10]))

(defn find-prime-dividers
  ([n primes]
   (loop [i (peek primes)
          found []
          primes (pop primes)]
     (if (or (= i nil) (= (count primes) 0) (>= i n))
       found
       (recur (peek primes)
              (if (= 0 (mod n i)) (conj found i) found)
              (pop primes))))))

(defn find-p
  ([n primes] (let [prime-dividers (find-prime-dividers n primes)]
                (find-p n prime-dividers (into #{} prime-dividers) #{})))
  ([n primes primes-set result]
   (if (or (contains? primes-set n) (contains? result n))
     (conj result n)
     (loop [item (peek primes) primes-to-check (pop primes) result result]
       (if (= 0 (count primes-to-check))
         result
         (if (= 0 (mod n item))
           (recur (peek primes-to-check) (pop primes-to-check) (conj (find-p (/ n item) primes primes-set result) (/ n item)))
           (recur (peek primes-to-check) (pop primes-to-check) result)))))))

(defn stupid-search [n]
  (loop [i 1 result []]
    (if (< i n)
      (if (= 0 (mod n i))
        (recur (+ i 1) (conj result i))
        (recur (+ i 1) result))
      result)))

(defn solve [opts]
  (let [primes (->> (problem-10/find-primes ('n opts))
                    sort
                    (into '())
                    (into '()))
        n ('n opts)
        nums (loop [i 3 result {}]
               (if (>= i n)
                 result
                 (recur (+ 1 i) (into result {i (+ 1 (reduce + (find-p i primes)))}))))
        pairs (filter (fn [entry]
                        (let [num (first entry)
                              dividers (second entry)
                              on-check (get nums dividers)]
                          (and (not (= num dividers)) (= on-check num)))) nums)]
    (reduce + (map first pairs))))

(defn solve-easy [opts]
  (let [n ('n opts)
        nums (loop [i 3 result {}]
               (if (>= i n)
                 result
                 (recur (+ 1 i) (into result {i (reduce + (stupid-search i))}))))
        pairs (filter (fn [entry]
                        (let [num (first entry) dividers (second entry)]
                          (and (not (= num dividers)) (= (get nums dividers) num)))) nums)]
    (reduce + (map first pairs))))