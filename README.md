<h1 align="center">
  <img src="https://raw.githubusercontent.com/gruns/icecream/master/logo.svg" width="220px" height="370px" alt="icecream">
</h1>


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

```clojure
(binding [icecream/enabled false]
  (ic 1))
;; => 1

(binding [icecream/prefix "hello: "]
  (ic 1))
;; "hello: 1"
;; => 1


(require '[tick.alpha.api :as t])
(binding [icecream/prefix #(str (inst-ms (t/now)) "| ")]
  (ic 1))
;; "1619103609280| 1"
;; => 1
```
