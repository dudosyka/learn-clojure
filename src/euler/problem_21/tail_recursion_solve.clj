(ns euler.problem-21.tail-recursion-solve)

(defn dividers
  ([n] (dividers n 2 0))
  ([n cur dividers]
   (if (> cur (Math/ceil (/ n 2)))
     (+ dividers 1)
     (recur n (inc cur) (+ dividers (* (if (zero? (mod n cur)) 1 0) cur))))))

(defn solve
  ([n] (solve n 1 {}))
  ([n cur nums]
   (if (>= cur n)
     (solve n 1 nums 0)
     (recur n (inc cur) (into nums {cur (dividers cur)}))))
  ([n cur nums friendlies]
   (if (>= cur n)
     friendlies
     (let [dividers (get nums cur)
           on-check (get nums dividers)]
       (recur n (inc cur) nums (+ (* (if (and (= cur on-check) (not= dividers cur)) 1 0) cur) friendlies))))))

(solve 10000)
