(ns euler.problem-10)

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

(defn find-primes
  ([n] (find-primes n 2 (get-n-size-map n)))
  ([n cur values]
   (if (<= cur (Math/ceil (Math/sqrt n)))
     (recur n (+ cur 1) (if (get values cur)
                          (remove-divides cur values)
                          values))
     (conj (mapv first (filter second values)) 1))))

(defn sum-primes
  ([n] (reduce + (find-primes n))))

(defn solve [opts]
  (sum-primes ('n opts)))