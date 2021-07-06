(ns icecream.icecream
  (:require [clojure.string :as string]))


(declare scalar? passed-symbol?
         format-value
         get-call-context)



;; CONF

(def ^:dynamic *enabled*
  true)

(def ^:dynamic *prefix*
  "ic| ")

(def ^:dynamic *output-function*
  prn)

(def ^:dynamic *include-context*
  true)



;; IMPL

(defmacro ic [form]
  (let [is-symbol (passed-symbol? form)
        do-display-expr (or is-symbol (scalar? form))
        prfx (if (fn? *prefix*)
               (*prefix*)
               *prefix*)
        ctx-prfx (when *include-context*
                   (let [{:keys [file line ns function]} (get-call-context)]
                     (str file ":" line " in " ns "/" function "- ")))]
    `(let [ic-val# ~form]
       (when *enabled*
         (*output-function*
          (str ~prfx ~ctx-prfx
               (if ~do-display-expr
                 (format-value ic-val#)
                 (str '~form ": " (format-value ic-val#))))))
       ic-val#)))



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

(defn format-value [v]
  (cond
    (nil? v) "nil"
    (string? v) (str "\"" v "\"")
    (symbol? v) (str "'" v)
    :else v))



;; HELPERS - INTROSPECTION

#?(:clj
   (defn- get-call-context []
     (let [stacktrace  (-> (Throwable.) .getStackTrace)]
       (some
        (fn [e]
          (let [class  (.getClassName e)
                method (.getMethodName e)
                file   (.getFileName e)
                line   (.getLineNumber e)
                [ns function]     (string/split class #"\$")]
            (when (and function
                       (not= class "icecream.icecream$ic")
                       (not= class "icecream.icecream$get_call_context")
                       (not= ns    "clojure.lang.Compiler")
                       (not= ns    "clojure.core")
                       (not= ns    "user"))
              {:file     file
               :line     line
               :ns       ns
               :function function})))
        stacktrace)))

   :cljs
   (defn- get-call-context []
     (let [stacktrace  (string/split (-> (js/Error.) .-stack) #"\n")]
       (some
        (fn [e]
          (let [[code-location file-location] (string/split e #"@")
                split-code-location (string/split code-location #"\$")
                [ns function] (if (< 2 (count split-code-location))
                                [(string/join "." (butlast split-code-location)) (last split-code-location)]
                                [(first split-code-location) nil])
                split-file-location (string/split file-location #":")
                file (string/join ":" (drop-last 2 split-file-location))
                ;; _column (last split-file-location)
                line (last (butlast split-file-location))]
            (when (and function
                       (not= [ns function] ["icecream.icecream" "ic"])
                       (not= [ns function] ["icecream.icecream" "get_call_context"]))
              {:file     file
               :line     line
               :ns       ns
               :function function})))
        stacktrace)))

   :default
   (throw (ex-info "Platform not supported" {:ex-type :unexpected-platform})))
