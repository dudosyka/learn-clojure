(ns euler.problem-10.loop-solve)

(defn n-size-map [n]
  (loop [i 2 arr {}]
    (if (<= i n)
      (recur (inc i) (into arr {i true}))
      arr)))

(defn remove-divides [n map]
  (loop [i (* n n) new-map map size (inc (count map))]
    (if (<= i size)
      (recur (+ i n) (assoc new-map i false) size)
      new-map)))

(defn find-primes-sum [n]
  (loop [cur 2 values (n-size-map n)]
    (if (<= cur (Math/ceil (Math/sqrt n)))
      (recur (inc cur) (if (get values cur)
                         (remove-divides cur values)
                         values))
      (loop [cur 1 result 1]
        (if (get values cur)
          (recur (inc cur) (+ result cur))
          (if (<= cur (count values))
            (recur (inc cur) result)
            (dec result)))))))

(defn solve [n]
  (find-primes-sum n))
