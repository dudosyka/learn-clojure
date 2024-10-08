(ns rb-dict.core
  (:refer-clojure :exclude [merge map filter keys into get conj])
  (:require [rb-dict.tree :as rb-tree])
  (:import (clojure.lang IFn IPersistentMap)))

(defprotocol IDict
  (get [this key])
  (add [this key value])
  (delete [this key])
  (conj [this ^IDict dict])
  (into [this sequence])
  (reduce-right [this start func])
  (reduce-left [this start func])
  (map [this func])
  (filter [this func])
  (values [this])
  (keys [this]))

(deftype RBDict [rb-tree]
  IDict
  (get [_ key] (rb-tree/find-value rb-tree key))
  (add [_ key value] (RBDict. (rb-tree/insert rb-tree key value)))
  (delete [_ key] (RBDict. (rb-tree/delete rb-tree key)))
  (conj [_ dict] (RBDict. (rb-tree/merge rb-tree dict)))
  (into [this sequence] (reduce (fn [acc [k v]] (add acc k v)) this sequence))
  (reduce-right [_ start func] (rb-tree/entries-reduced rb-tree start func :right))
  (reduce-left [_ start func] (rb-tree/entries-reduced rb-tree start func :left))
  (map [_ func] (rb-tree/entries-mapped rb-tree func))
  (filter [_ func] (rb-tree/entries-filtered rb-tree func))
  (values [_] (rb-tree/entries-mapped rb-tree second))
  (keys [_] (rb-tree/entries-mapped rb-tree first))
  IFn
  (invoke [_ key]
    (get _ key))
  (invoke [_ key value]
    (add _ key value))
  IPersistentMap
  (equiv [this obj]
    (= (seq this) (seq obj)))
  (seq [_]
    (clojure.core/into '() (reverse (rb-tree/entries rb-tree))))
  rb-dict.tree/MergableValue
  (merge [_ dict]
    (RBDict. (rb-tree/merge rb-tree (.-rb-tree dict)))))

(defn rb-dict [& values]
  (if (not (even? (count values)))
    (throw (Exception. "Bad values count (must be even)"))
    (into (RBDict. nil) (partition 2 values))))

(def dict (rb-dict 1 (String. "a")
                   2 {:f "b" :g (rb-dict 3 4)}
                   3 "c"
                   7 '(1 23)
                   5 [1 2 3]
                   4 "d"))