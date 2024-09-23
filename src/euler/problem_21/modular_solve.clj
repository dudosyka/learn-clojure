(ns euler.problem-21.modular-solve)

(defn dividers [n]
  (reduce + (filter #(zero? (mod n %)) (take (Math/ceil (/ n 2)) (iterate inc 1)))))

(defn solve [n]
  (let [with-dividers (reduce into (map (fn [x] {x (dividers x)}) (take n (iterate inc 2))))]
    (reduce + (map first (filter (fn [entry]
                                   (let [num (first entry)
                                         dividers (second entry)
                                         on-check (get with-dividers dividers)]
                                     (and (= num on-check) (not= dividers num)))) with-dividers)))))
