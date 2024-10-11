(ns stream.cli
  (:require [clojure.string :refer [split trim]])
  (:require [stream.reader :refer [read-batched]])
  (:require [stream.interpolator :refer [interpolate describe]]))

(defrecord Command [name usage docstring parse default required?])

(def options {'--help (->Command "help" "--help" "Display help message"
                                 nil nil false)
              '-w     (->Command "-w" "-w N" "Size of the interpolation window"
                                 #(parse-long %) "4" true)
              '-x     (->Command "-x" "-x X" "Value of x to find function in (f(x) = ?)"
                                 #(parse-double %) nil true)
              '-a     (->Command "-a" "-a ALGO1, ALGO2" "Interpolation algorithm(s)"
                                 #(map trim (split % #",")) ["lagrange"] true)})

(defn- display-usage []
  (println "Usage: cli <args>")
  (println "Available args: ")
  (doseq [[_ v] options]
    (println (str (:name v) ": "
                  (:usage v) ", "
                  (:docstring v) " "
                  (if (:required? v) " -- Required" ""))))
  (println "You must provide at least -x arg to start"))

(defn parse-options [opts]
  (let [required (->> options
                      (filter #(:required? (second %)))
                      (mapv first))
        defaults (->> options
                      (filter #(not (nil? (:default (second %)))))
                      (map (fn [[k v]] [k (:default v)]))
                      (into {}))
        opts (conj defaults opts)
        required-opts-size (->> required
                                (map #(get opts %))
                                (filter #(not (nil? %)))
                                (count))]
    (if (< (count required) required-opts-size)
      (display-usage)
      (try
        (into {} (map (fn [[k v]]
                        (let [option (get options k)]
                          (when (nil? option)
                            (throw (Exception. "Bad args")))
                          [k ((:parse option) v)])) opts))
        (catch Exception _ (display-usage))))))

(defn run [opts]
  (let [opts (parse-options opts)]
    (if (contains? opts '--help)
      (display-usage)
      ((fn [init]
         (let [batched (read-batched ('-w opts) init)]
           (doseq [algo ('-a opts)]
             (println (describe algo (interpolate algo batched ('-x opts)))))
           (recur batched))) '()))))

(run {'-w "4" '-x "4.04" '-a "lagrange, linear"})