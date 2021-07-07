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
;; disable
(binding [icecream/*enabled* false]
  (ic 1))
;; => 1

;; custom prefix - constant
(binding [icecream/*prefix* "hello: "]
  (ic 1))
;; "hello: 1"
;; => 1

;; custom prefix - function
(require '[tick.alpha.api :as t])
(binding [icecream/*prefix* #(str (inst-ms (t/now)) "| ")]
  (ic 1))
;; "1619103609280| 1"
;; => 1

;; systematically include call context
(binding [icecream/*include-context* true]
  (test 1))
;; "ic| form-init4003934083635828251.clj:6 in icecream-test.core/eval51124- 1
;; => 1

;; smartly include call context (only when inside function calls)
(binding [icecream/*include-context* :smart]
  (ic 1))
;; "ic| 1"
;; => 1

(defn test [v]
  (ic v))
(binding [icecream/*include-context* :smart]
  (test 1))
;; "ic| core.clj:6 in icecream-test.core/test- 1
;; => 1
(binding [icecream/*include-context* false]
  (test 1))
;; "ic| 1
;; => 1

;; when calling `ic` w/ no args, `*include-context*` is systematically considered true
(ic)
;; "ic| form-init4003934083635828251.clj:6 in icecream-test.core/eval51125
;; => nil
```


## Alternatives

[Tupelo](https://github.com/cloojure/tupelo) provides [spyx](https://cljdoc.org/d/tupelo/tupelo/0.9.197/api/tupelo.core#spyx) that behaves very close to `ic` but for which the output function (`println`) can't be redefined.

It doesn't print caller info (`*include-context*` config). Tupelo nethertheless provides [fn-info](https://cljdoc.org/d/tupelo/tupelo/0.9.197/api/tupelo.misc#fn-info) and [fn-info-caller](https://cljdoc.org/d/tupelo/tupelo/0.9.197/api/tupelo.misc#fn-info-caller) (Clojure only).
