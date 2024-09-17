(ns core)

(defn compute-with-timer [f]
  (let [startAt (System/currentTimeMillis)]
    (println (f))
    (let [computationTime (- (System/currentTimeMillis) startAt)]
      (println "Completed in: " computationTime "ms"))))

(defn run [opts]
  (println opts))