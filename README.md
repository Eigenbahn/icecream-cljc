# icecream-cljc

Port of [gruns/icecream](https://github.com/gruns/icecream) to Clojure(Script).


## Usage

```clojure
(require '[icecream.icecream :as icecream :refer [ic]])

;; scalars

(ic "hello")
;; "ic| "hello""
;; => "hello"

(ic nil)
;; "ic| nil"
;; => nil

(ic 1)
;; "ic| 1"
;; => 1

(ic :a)
;; "ic| :a"
;; => :a

(ic 'a)
;; "ic| 'a"
;; => a

;; works on functions
(ic (+ 1 1))
;; "ic| (+ 1 1): 2"
;; => 2

;; works on special forms
(ic (if true :yes :no))
;; "ic| (if true :yes :no): :yes"
;; => :yes

;; works on macros
(ic (when true :yes))
;; "ic| (when true :yes): :yes"
;; => :yes


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
