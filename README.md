# Лабораторная работа №2

```
Выполнил: Шляпников Александр P3322

Вариант: rb-dict
```

## Требования к разрабатываемой структуре данных

1. Структура данных должна быть моноидом
2. Структура данных должна быть неизменяемой
3. Структура должна быть полиморфной
4. Должны быть реализованы функции:
    1. Добавление \ Удаление элементов
    2. Фильтрация
    3. Отображение
    4. Левая и правая свертки
5. Должен быть реализован набор unit-тестов
6. Должен быть реализован набор property-based тестов 
(не менее 3х тестов, в том числе описывающих спецификацию структуры как моноида)

## Реализация

### Реализация красно-черного дерева
``` ./src/rb-dict/tree.clj ```

Красно-черное дерево по своей сути это бинарное дерево поиска, в котором каждая вершина либо черная, либо красная. 
Благодаря раскраске, а также ограничениям на основе этой раскраски эта структура позволяет получить бинарное дерево, 
которое в среднем работает за O(logn).

Ограничения накладываемые на дерево:
1. Черная высота дерево одинакова в обоих поддеревьях
2. Корень всегда черный
3. Оба потомка красной вершины всегда черные

Для соблюдения этих ограничений потребовалось реализовать функции балансировки при добавлении и удалении элементов

```clojure
; Балансировка после добавления элемента
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

```

При добавлении элемента балансировка требуется в случае, если мы добавляем новый элемент к красной вершине
в таком случае мы взависимости от цвета родственников либо просто поднимаем красный цвет на уровень выше, 
либо начинаем поворачивать дерево

В случае с удаление балансировка нужна, когда удаляется черный лист без детей, в этом случае изменяется черная высота
поэтому требуется балансироваться, в любом другом случае элемент может быть удален без изменения высоты, а потому не требует балансировки

```clojure
; Балансировка после удаления элемента
(defn- balance-after-delete [parent parent-side]
   (let [vertex (get parent parent-side)
         sibling-side (opposite parent-side)
         sibling (get parent sibling-side)
         same-side-child (get sibling parent-side)]
      ; В зависимости от цветов у родственников выполняем различные операции для балансировки
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
```

Согласно требованиям (свойство моноида) дерево должно было также поддерживать операцию слияния с другим деревом

Для этого был объявлен протокол, описывающий интерфейс операции слияния, а также добавлена реализация этого протокола для основных типов данных

```clojure

(defprotocol MergableValue
  (merge [v1 v2]))

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

```

Для работы функций filter \ map \ reduce требовалось также реализовать возможность обхода дерева по часовой и против часовой стрелки,
для того чтобы эти операции можно было комбинировать со стандартным словарем в clojure был добавлен интерфейс Sequential и его реалзация для IPersistentMap и RBTreeVertex

```clojure

(defprotocol Sequential
   (entries [this])
   (entries-mapped [this f])
   (entries-filtered [this f])
   (entries-reduced [this start f side]))

(extend-protocol Sequential
  IPersistentMap
  (entries [this] (seq this))
  (entries-filtered [this f] (filter f this))
  (entries-mapped [this f] (map f this))
  RBTreeVertex
  (entries-mapped [this f]
    (into [] (concat
      (entries-mapped (:left this) f)
      [(f [(:key this) (:value this)])]
      (entries-mapped (:right this) f))))
  (entries [this] (entries-mapped this identity))
  (entries-filtered [this f]
    (let [entry [(:key this) (:value this)]]
       (into [] (concat
                 (entries-filtered (:left this) f)
                 (if (f entry) [entry] [])
                 (entries-filtered (:right this) f)))))
  (entries-reduced [this start f side]
    (entries-reduced
     (get this (opposite side))
     (f (entries-reduced (get this side) start f side) [(:key this) (:value this)])
     f side))
  ; Реализация для nil позволяет не делать проверок в рамках методов на то, что перебор вершин дошел до листа
  nil
  (entries [_] [])
  (entries-filtered [_ _] [])
  (entries-mapped [_ _] [])
  (entries-reduced [_ start _ _] start))

```

Все остальные операции не включают какой либо специфики в своей реализации и работают также как с любым другим бинарным деревом поиска.

### Реализация интерфейса
``` ./src/rb-dict/core.clj ```

Для описания интерфейса был объявлен протокол IDict

```clojure

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

```

А также тип реализующий данный протокол, а также ряд других для поддержки необходимых операций

```clojure

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
  ; Необходимо для поддержки вызовов (rb-dict key) (rb-dict key value) для операций вставки и поиска
  IFn
  (invoke [_ key]
    (get _ key))
  (invoke [_ key value]
    (add _ key value))
  ; Необходимо для поддержки стандартной операции сравнения, "красивого" принта, слияния и сравнения со стандартным словарем clojure 
  IPersistentMap
  (equiv [this obj]
    (= (seq this) (seq obj)))
  (seq [_]
    (clojure.core/into '() (reverse (rb-tree/entries rb-tree))))
  ; Необходимо для поддержки слияния с другими rb-dict
  rb-dict.tree/MergableValue
  (merge [_ dict]
    (RBDict. (rb-tree/merge rb-tree (.-rb-tree dict)))))

```

### Реализация unit-тестирования
``` ./test/rb_tree_unit_test.clj ```

Были написаны unit тесты для каждой из реализованых в интерфейсе функций, рассмотрены краевые случаи. В рамках реализации не были использованы какие то специфические, требующие пояснений методы

### Реализация property-based тестирования
``` ./src/rb_tree_prop_test.clj ```

Для написания property-based тестирования были использваны генераторы и раннер из пакета test.check
Всего было проверено 3 свойства структуры

##### Свойство моноида - коммутативность операции слияния

```clojure
(deftest test-commutative
  (testing "Test that conjunction operations is commutative"
    (is (check/quick-check
         100
         (prop/for-all [dict gen-dict
                        second-dict gen-dict]
                       (= (conj dict second-dict) (conj second-dict dict)))))))
```

##### Свойство моноида - операция слияния с нейтральным элементом не меняет структуру 

```clojure
(deftest test-neutral-element
  (testing "Test operations with neutral element"
    (is (check/quick-check
         100
         (prop/for-all [dict gen-dict]
                       (= (conj dict (rb-dict)) dict))))
    (is (check/quick-check
         100
         (prop/for-all [dict gen-dict]
                       (= (conj (rb-dict) dict) dict))))))
```

##### Свойство красно-черного дерева - у красной вершины 2 черных сына

```clojure
(defn check-valid-node [node]
  (if (black? node)
    true
    (and
     (= (red-child node) nil)
     (or
      (= (child-count node) 2)
      (= (child-count node) 0)))))

(defn check-no-red-vertex-with-red-sons [tree]
  (and
   (check-no-red-vertex-with-red-sons (:left dict))
   (check-valid-node tree)
   (check-no-red-vertex-with-red-sons (:right dict))))

(deftest test-no-red-vertex-with-red-sons
  (testing "Test that every red vertex have two black sons"
    (is (check/quick-check
         100
         (prop/for-all [dict gen-dict]
                       (= true (check-no-red-vertex-with-red-sons dict)))))))
```

## Вывод

В рамках работы получилось ещё глубже покрузиться в язык, частности получилось разобраться:

1. С работой с абстракциями, в частности описание типов, протоколов их реализации, расширения стандартных типов языка. Стала ясна логика построения структур данных в clojure, как при помощи него описать какую-то предметную область.
2. С рекурсией... С ней в принципе было все понятно, но было интересно описывать операции обхода и манипуляции с элементами дерева именно при помощи рекурсии, такой способ позволяет, на мой взгляд, более деклоративно подходить к описанию поведения того или иного объекта.
3. С тредами, речь про функции -> ->> опять же, концепция так же, как и с рекурсией была ясна и до этого, но работа дала понять насколько это мощный инструмент для работы с неизменяемыми типами, не нужно зарываться в несколько уровней сложности, можно просто описать последовательность действий.
4. С написанием тестов, в частности с property-based тестами, до этого в работе данный подход не использовал, мне он показался очень полезным, позволяет четко и понятно описывать ограничения предъявляемые к структурам, не вникая в то, с какими именно данными эти структуры работают.

Работа интересная и полезная.