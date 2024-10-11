(ns stream.interpolator)

(defmulti interpolate (fn [type _ _] type))

(defn- calc-polynomial-for-point [point points target]
  (reduce (fn [acc cur]
            (if (= point cur)
              acc
              (* acc (/ (- target (:x cur)) (- (:x point) (:x cur)))))) 1 points))

(defmethod interpolate "lagrange" [_ points target]
  (reduce (fn [acc point]
            (let [poly (calc-polynomial-for-point point points target)]
              (+ acc (* (:y point) poly)))) 0 points))

(defmethod interpolate "linear" [_ points target]
  ((fn [points cur left]
     (if (nil? cur)
       nil
       (if (> target (:x cur))
         (recur (drop 1 points) (first points) cur)
         (if (< target (:x cur))
           (if (nil? left)
             nil
             (+ (:y left) (* (/ (- target (:x left)) (- (:x cur) (:x left))) (- (:y cur) (:y left)))))
           (if (= target (:x cur))
             (:y cur)
             nil))))) (drop 1 points) (first points) nil))

(defmulti describe (fn [t _] t))

(defn- process-result [result]
  (if (nil? result)
    "Not enough data in window"
    result))

(defmethod describe "lagrange" [_ result]
  (str "Interpolated by Lagrange: " (process-result result)))

(defmethod describe "linear" [_ result]
  (str "Interpolated by Linear: " (process-result result)))