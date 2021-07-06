(ns icecream.icecream)


(declare scalar? passed-symbol?
         format-value)



;; CONF

(def ^:dynamic enabled
  true)

(def ^:dynamic prefix
  "ic| ")

(def ^:dynamic output-function
  prn)



;; IMPL

(defmacro ic [form]
  (let [r (eval form)
        is-symbol (passed-symbol? form)
        prfx (if (fn? prefix)
               (prefix)
               prefix)]
    (when enabled
      (output-function
       (str prfx
            (if (or is-symbol (scalar? form))
              (format-value r)
              (str form ": " (format-value r))))))
    (if is-symbol
      form
      r)))



;; HELPERS - CORE

;; NB: not including `symbol?` / `ident?` as it would trigger for vars passed to the macro.
;; Passed symbols would appear as `(quote <symbol>)`.
;; That's why we have the `passed-symbol?` predicate.

(defn- scalar? [v]
  (some #(% v) [nil?
                number?
                boolean?
                string?
                keyword?]))

(defn- passed-symbol? [v]
  (and (seq? v)
       (= 2 (count v))
       (= 'quote (first v))
       (symbol? (last v))))



;; HELPERS - VALUE FORMAT

(defn- format-value [v]
  (cond
    (nil? v) "nil"
    (string? v) (str "\"" v "\"")
    (symbol? v) (str "'" v)
    :else v))
