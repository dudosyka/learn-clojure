(ns rb-tree.vertex
  (:refer-clojure :exclude [merge])
  (:require [clojure.core.match :refer [match]])
  (:import (clojure.lang IPersistentCollection IPersistentMap)))

(defprotocol MergableValue
  (merge [v1 v2]))
(defprotocol Sequential
  (entries [this])
  (entries-mapped [this f])
  (entries-filtered [this f])
  (entries-reduced [this start f side]))
(defrecord RBTreeVertex [color key value right left])

(defn- color [vertex]
  (if (nil? vertex)
    :black
    (:color vertex)))
(defn- red? [vertex]
  (if (nil? vertex)
    false
    (= (:color vertex) :red)))
(defn- red-child [vertex] (->> [:right :left]
                               (filter
                                (fn [son-side]
                                  (let [son (get vertex son-side)]
                                    (and (some? son) (red? son)))))
                               (first)))
(defn- make-black [vertex]
  (if (nil? vertex)
    vertex
    (assoc vertex :color :black)))
(defn- make-red [vertex]
  (if (nil? vertex)
    vertex
    (assoc vertex :color :red)))

(defn- grater [k1 k2]
  (> (compare k1 k2) 0))
(defn- eq [k1 k2]
  (= (compare k1 k2) 0))

(defn- opposite [side] (if (= side :left) :right :left))
(defn- child-count [vertex]
  (->> [:right :left]
       (filter #(not (nil? (get vertex %))))
       (count)))
(defn- append
  ([vertex side k v] (assoc vertex side (->RBTreeVertex :red k v nil nil)))
  ([vertex side target] (assoc vertex side target)))
(defn- turn [vertex son side]
  (let [new-son (-> vertex
                    (append (opposite side) (get son side))
                    (make-red))
        new-vertex (-> son
                       (append side new-son)
                       (make-black))]
    new-vertex))
(defn- big-turn [vertex son grand-son side]
  (let [new-grand-son (append son (opposite side) (get grand-son side))
        new-son (append grand-son side new-grand-son)
        new-vertex (append vertex side new-son)]
    (turn new-vertex new-son (opposite side))))

(defn- balance-after-insert [parent current-side current]
  (let [fb-failed? (and (red? current) (some? (red-child current)))]
    (if (not fb-failed?)
      (append parent current-side current)
      (let [son-side (red-child current)
            grand-son (get current son-side)
            sibling-side (opposite current-side)
            sibling (get parent sibling-side)]
        (if (and (some? sibling) (red? sibling))
          (-> parent
              (make-red)
              (append sibling-side (make-black sibling))
              (append current-side (make-black current)))
          (if (= current-side son-side)
            (turn parent current sibling-side)
            (big-turn parent current grand-son current-side)))))))
(defn- find-max [subtree]
  (if (nil? (:right subtree))
    subtree
    (recur (:right subtree))))
(defn- delete-leaf [vertex]
  (match [(color vertex) (child-count vertex)]
    [:red 0] [nil false]
    [:black 1] [(make-black (if (some? (:right vertex))
                              (:right vertex)
                              (:left vertex)))
                false]
    [:black 0] [nil true]))
(defn- balance-after-delete [parent parent-side]
  (let [vertex (get parent parent-side)
        sibling-side (opposite parent-side)
        sibling (get parent sibling-side)
        same-side-child (get sibling parent-side)]
    (match [(color sibling) (color (get sibling sibling-side)) (color same-side-child)]
      [:black :red _]
      (let [turned (turn parent sibling parent-side)]
        (-> turned
            (assoc :color (color parent))
            (append sibling-side (make-black (get turned sibling-side)))
            (append parent-side (make-black (get turned parent-side)))))

      [:black :black :red]
      (let [turned (-> parent
                       (append sibling-side
                               (-> same-side-child
                                   (make-black)
                                   (append sibling-side
                                           (-> sibling
                                               (make-red)
                                               (append parent-side (get same-side-child sibling-side))))))
                       (append parent-side vertex))]
        (balance-after-delete turned parent-side))

      [:black :black :black]
      (-> parent
          (make-black)
          (append sibling-side (make-red sibling)))

      [:red _ _]
      (let [turned (turn parent sibling parent-side)
            color-fixed (-> turned
                            (make-black)
                            (append parent-side (make-red (get turned parent-side))))
            fixed (balance-after-delete (get color-fixed parent-side) parent-side)]
        (append color-fixed parent-side fixed)))))

(defn create [key value]
  (->RBTreeVertex :black key value nil nil))
(defn delete
  ([vertex key]
   (if (some? vertex)
     (let [[tree _] (delete vertex key nil nil)]
       tree)
     nil))
  ([vertex key parent parent-side]
   (let [[updated need-balance] (if (nil? vertex)
                                  [nil nil]
                                  (if (eq (:key vertex) key)
                                    (if (= (child-count vertex) 2)
                                      (let [subtree-max (find-max (:left vertex))
                                            vertex (-> vertex
                                                       (assoc :value (:value subtree-max))
                                                       (assoc :key (:key subtree-max)))]
                                        (delete (:left vertex) (:key subtree-max) vertex :left))
                                      (delete-leaf vertex))
                                    (if (grater key (:key vertex))
                                      (delete (:right vertex) key vertex :right)
                                      (delete (:left vertex) key vertex :left))))
         next (if (some? parent)
                (append parent parent-side updated)
                updated)]
     (if (and need-balance (some? parent-side))
       (let [fixed (balance-after-delete next parent-side)]
         ; Нам надо проверять что ключи совпадают, потому что если мы обрабатывали случай
         ; с красным братом перебалансировка уходит на уровни ниже и имеет право вернуть
         ; черный узел на замену текущему родителю, если он из поддерева т.е. другой родительский узел
         [fixed (and (eq (:key next) (:key fixed)) (= (color next) (color fixed) :black))])
       [(if (nil? parent-side) (make-black next) next) false]))))
(defn insert
  ([vertex key value] (insert vertex key value true))
  ([vertex key value replace]
   (if (some? vertex)
     (if (not (= (type (:key vertex)) (type key)))
       (throw (Exception. "Incomparable keys"))
       (let [res (insert vertex nil nil key value replace)]
         res))
     (create key value)))
  ([vertex parent-side parent key value replace]
   (let [side (if (grater (:key vertex) key) :left :right)
         son (get vertex side)
         updated (if (eq (:key vertex) key)
                   (assoc vertex :value (if (or replace (not= (type value) (type (:value vertex))))
                                          value
                                          (merge (:value vertex) value)))
                   (if (nil? son)
                     (append vertex side key value)
                     (insert son side vertex key value replace)))]

     (if (some? parent)
       (balance-after-insert parent parent-side updated)
       (make-black updated)))))
(defn find-value [vertex key]
  (if (nil? vertex)
    nil
    (if (eq (:key vertex) key)
      (:value vertex)
      (if (grater key (:key vertex))
        (recur (:right vertex) key)
        (recur (:left vertex) key)))))

(extend-protocol Sequential
  IPersistentMap
  (entries [this] (seq this))
  (entries-filtered [this f] (filter f this))
  (entries-mapped [this f] (map f this))
  RBTreeVertex
  (entries-mapped [this f]
    (if (nil? this) [] (into [] (concat
                                 (entries-mapped (:right this) f)
                                 [(f [(:key this) (:value this)])]
                                 (entries-mapped (:left this) f)))))
  (entries [this] (entries-mapped this identity))
  (entries-filtered [this f]
    (if (nil? this) [] (let [entry [(:key this) (:value this)]]
                         (into [] (concat
                                   (entries-filtered (:left this) f)
                                   (if (f entry) [entry] [])
                                   (entries-filtered (:right this) f))))))
  (entries-reduced [this start f side]
    (entries-reduced
     (get this (opposite side))
     (f (entries-reduced (get this side) start f side) [(:key this) (:value this)])
     f side)))

(extend-type nil
  Sequential
  (entries [_] [])
  (entries-filtered [_ _] [])
  (entries-mapped [_ _] [])
  (entries-reduced [_ start _ _] start))

(extend-protocol MergableValue
  Number
  (merge [v1 v2]
    (+ v1 v2))
  String
  (merge [v1 v2]
    (str v1 v2))
  IPersistentCollection
  (merge [v1 v2]
    (into v1 v2))
  RBTreeVertex
  (merge [v1 v2]
    (loop [entries (entries v2)
           v1 v1]
      (if (some? (peek entries))
        (let [[k v] (peek entries)]
          (recur (pop entries) (insert v1 k v false)))
        v1))))

; Тут мы проверяем как будет работать удаление вершины (слева)
; у которой есть ЧЕРНЫЙ БРАТ с красным СЫНОМ (с ДРУГОЙ СТОРОНЫ от удаляемого - справа) (удаление 25 элемента)
;
; А затем проверяем как будет работать удаление,
; если у ЧЕРНОГО БРАТА ОБА ребенка ЧЕРНЫЕ
; (удаление элемента 12)
;
; А затем снова проверяем как будет работать удаление,
; если у ЧЕРНОГО БРАТА ОБА ребенка ЧЕРНЫЕ
; (удаление элемента 20) В рамках этого удаления также проверяется
; корректность работы рекурсивной перебалансировки +
; при рекурсивном переходе к перебалансировки по ключу 19 идёт использование
; ЧЕРНЫЙ БРАТ с красным СЫНОМ (с ТОЙ ЖЕ СТОРОНЫ)
;(def another-tree (delete-recursive (build-rb '(2 3 4 5 10 11 12 14 15 16 17 19 20 23 25 30 22)) 30))
;(def prepared (delete-recursive (delete-recursive another-tree 25) 12))
;(render (delete-recursive prepared 20))

;(def red-brother-tree (build-rb '(2 3 4 5 10 11 12 14 15 16)))
;(def merged (merge red-brother-tree red-brother-tree))
;(println (as-list red-brother-tree))
;(println (as-list merged))
;(def with-subtree (insert red-brother-tree 10 red-brother-tree))
;
;(def with-subtree (->(build-rb '(2 3 4 5 10 11 12 14))
;                     (insert 12 with-subtree)))
;
;
;(def with-subtree2 (->(build-rb '(2 3 4 5 10 11 12 14))
;                      (insert 12 with-subtree)
;                      (insert 10 (insert red-brother-tree 10 red-brother-tree))))

;(println (as-list with-subtree (fn [[k v]] (if (= (type v) rb_tree_dict.internal.RBTreeVertex) [k (as-list v)] [k v]))))
;
;(def merged-subtree (merge with-subtree with-subtree))
;(println (render merged-subtree))

;(render red-brother-tree)
;(render (insert red-brother-tree 10 34))
;
;(println "---------------------------------")
; {:color :black,
; :key 3,
; :value 1,
; :right
; {:color :red,
;  :key 14,
;  :value 1,
;  :right {:color :black, :key 16, :value 1, :right nil, :left nil},
;  :left
;  {:color :black,
;   :key 5,
;   :value 1,
;   :right {:color :red, :key 12, :value 1, :right nil, :left nil},
;   :left nil}},
; :left {:color :black, :key 2, :value 1, :right nil, :left nil}}
; {:color :black,
; :key 3,
; :value 1,
; :right
; {:color :red,
;  :key 14,
;  :value 1,
;  :right {:color :black, :key 16, :value 1, :right nil, :left nil},
;  :left
;  {:color :black,
;   :key 5,
;   :value 1,
;   :right {:color :red, :key 12, :value 1, :right nil, :left nil},
;   :left nil}},
; :left {:color :black, :key 2, :value 1, :right nil, :left nil}}
;; Проверяем случай когда удаление идёт у КРАСНОГО БРАТА (удаляем 10 элемент, его красный брат - 14)
;(clojure.pprint/pprint (delete red-brother-tree 10))
;(println "---------------------------------")
;
;; Проверяем случай когда у черного узла красный сын (удаляем элемент 15, его красный сын - 16)
;(render (-> red-brother-tree
;            (delete-recursive 10)
;            (delete-recursive 15)))
;(println "---------------------------------")
;
;; Это проверка на ЧЕРНОГО БРАТА (ОБА СЫНА черные)
;; брат - 12 элемент он черный с двумя null черными сыновьями
;(render (-> red-brother-tree
;            (delete-recursive 10)
;            (delete-recursive 15)
;            (delete-recursive 16)))
;
;(println "---------------------------------")
;
;; Далее проверяем удаление красной вершины без детей это элемент 12
;(render (-> red-brother-tree
;            (delete-recursive 10)
;            (delete-recursive 15)
;            (delete-recursive 12)))