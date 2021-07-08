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

Smartly outputs scalars directly:

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

Behavior can be altered through the use of dynamic variables:

```clojure
;; disable
(binding [icecream/*enabled* false]
  (ic 1))
;; => 1

;; custom output function
(require '[taoensso.timbre :as timbre
           :refer [debug]])
(binding [icecream/*output-function* #(debug %)
          ;; NB: you typically want this one as well, explained later
          icecream/*include-file-location-in-context* false]
  (ic 1))
;; 21-07-07 20:01:03 homebase DEBUG [icecream-test.core:131] - ic| 1
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

;; same but don't include file name / line number
(binding [icecream/*include-context* true
          icecream/*include-file-location-in-context* false]
  (test 1))
;; "ic| icecream-test.core/eval51124- 1
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


## Limitations

Introspection (when `*include-context*` is `true` or `:smart`) won't work in some cases. Typically in multi-methods or when calling dynamically created lambda functions.


## Alternatives

#### tap

You might want to take a look at the native [tap](https://clojure.org/reference/repl_and_main#_tap) API.

This [blog post](https://quanttype.net/posts/2018-10-18-how-i-use-tap.html) describe how it can be used for debugging purposes.

It allows binding several print destinations (akin to iceream's `*output-function*` config). This way you could both log in the REPL and forward say [portal](https://github.com/djblue/portal).

One notable difference is that `tap>` doesn't return the evaluated form value, but instead a boolean indicating if the action to output succeeded. As such it cannot be inserted as easily as other solutions (inside function calls, threading macros...).

Have also a look at the convenient [pez/taplet](https://github.com/PEZ/taplet) that allows quickly tapping a whole let-binding vector.


#### spyscope

[spyscope](https://github.com/dgrnbrg/spyscope) provides several utilities similar to icecream but in the form of reader tags instead of macros.

Notably `#spy/d` is very close in behavior to `ic`, including context resolution.

Its output function cannot be customized.


#### tupelo's spyx

[tupelo](https://github.com/cloojure/tupelo) provides [spyx](https://cljdoc.org/d/tupelo/tupelo/0.9.197/api/tupelo.core#spyx) that behaves very close to `ic` but for which the output function (`println`) can't be redefined.

It doesn't print caller info (`*include-context*` config). tupelo nethertheless provides [fn-info](https://cljdoc.org/d/tupelo/tupelo/0.9.197/api/tupelo.misc#fn-info) and [fn-info-caller](https://cljdoc.org/d/tupelo/tupelo/0.9.197/api/tupelo.misc#fn-info-caller) (both Clojure only) that is very close in implementation to icecream's `get-call-context`.
