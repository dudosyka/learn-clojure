(ns euler.problem-21.modular-solve)

(defn dividers [n]
  (->> (iterate inc 1)
       (take (Math/ceil (/ n 2)))
       (filter #(zero? (mod n %)))
       (reduce +)))

(defn solve [n]
  (let [with-dividers (->> (iterate inc 2)
                           (take n)
                           (map (fn [x] {x (dividers x)}))
                           (reduce into))]

    (->> with-dividers
         (filter (fn [[num dividers]] (and (= num (get with-dividers dividers)) (not= dividers num))))
         (map first)
         (reduce +))))
