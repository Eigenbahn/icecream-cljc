<h1 align="center">
  <img src="https://raw.githubusercontent.com/gruns/icecream/master/logo.svg" width="220px" height="370px" alt="icecream">
</h1>


## icecream-cljc

Port of Python's [IceCream](https://github.com/gruns/icecream) to Clojure(Script).


## Usage

```clojure
(require '[icecream.icecream :as icecream :refer [ic]])

```

Works on functions, special forms and macros:

```clojure
;; function
(ic (+ 1 1))
;; "ic| (+ 1 1): 2"
;; => 2

;; special form
(ic (if true :yes :no))
;; "ic| (if true :yes :no): :yes"
;; => :yes

;; macro
(ic (when true :yes))
;; "ic| (when true :yes): :yes"
;; => :yes
```

Outputs scalars directly:

```clojure
(ic 1)
;; "ic| 1"
;; => 1

(ic :a)
;; "ic| :a"
;; => :a

(ic 'a)
;; "ic| 'a"
;; => a

(ic "hello")
;; "ic| "hello""
;; => "hello"

(ic nil)
;; "ic| nil"
;; => nil

```

## Configuration

Behaviour can be altered through the use of dynamic variables:

```clojure
(binding [icecream/*enabled* false]
  (ic 1))
;; => 1

(binding [icecream/*prefix* "hello: "]
  (ic 1))
;; "hello: 1"
;; => 1

(require '[tick.alpha.api :as t])
(binding [icecream/*prefix* #(str (inst-ms (t/now)) "| ")]
  (ic 1))
;; "1619103609280| 1"
;; => 1
```


## Alternatives

[Tupelo](https://github.com/cloojure/tupelo) provides [spyx](https://cljdoc.org/d/tupelo/tupelo/0.9.197/api/tupelo.core#spyx) that behaves just like `ic` when passed a S-exp.

It doesn't print caller info (`include-content` config). Tupelo nethertheless provides [fn-info](https://cljdoc.org/d/tupelo/tupelo/0.9.197/api/tupelo.misc#fn-info) and [fn-info-caller](https://cljdoc.org/d/tupelo/tupelo/0.9.197/api/tupelo.misc#fn-info-caller) (Clojure only).
