(ns core)

(defn compute-with-timer [f]
  (let [startAt (System/currentTimeMillis)
        result (f)
        computationTime (- (System/currentTimeMillis) startAt)]
    (println "Completed in: " computationTime "ms")
    result))

(defn run [opts]
  (println opts))