(ns stream.reader
  (:require [stream.point :refer [parse-from-line]]))

(defn read-batched [batch-size init]
  (let [point (parse-from-line (read-line))
        list (if (< (count init) batch-size) init (drop 1 init))
        new-list (concat list [point])]
    (if (= (count new-list) batch-size)
      new-list
      (read-batched batch-size new-list))))
