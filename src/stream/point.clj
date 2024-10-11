(ns stream.point
  (:require [clojure.string :refer [split]]))

(defrecord Point [x y])

(defn parse-from-line [line]
  ;(->Point (random-double 1 10) (random-double 1 10)))
  (let [split (split line #";")]
    (if (= (count split) 2)
      (->Point (parse-double (first split)) (parse-double (second split)))
      (throw (Exception. "Bad line provided")))))
