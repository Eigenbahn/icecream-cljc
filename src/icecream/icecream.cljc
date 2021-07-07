(ns icecream.icecream
  (:require [clojure.string :as string]))


(declare scalar? passed-symbol?
         format-value
         get-call-context
         deserialize-stacktrace-fn-name)



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
        do-display-expr (or is-symbol (scalar? form))]
    `(let [ic-val# ~form
           prfx# (if (fn? *prefix*)
                   (*prefix*)
                   *prefix*)
           call-ctx# (when icecream/*include-context*
                       (get-call-context))
           ctx-prfx# (when call-ctx#
                       (str (:file call-ctx#) ":" (:line call-ctx#)
                            " in " (:ns call-ctx#) "/" (deserialize-stacktrace-fn-name (:function call-ctx#)) "- "))]
       (when *enabled*
         (*output-function*
          (str prfx# ctx-prfx#
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
   (defn- parse-stacktrace-entry [e]
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
                  ;; (not= ns    "user")
                  ;; (not= ns    "clojure.main")
                  )
         {:file     file
          :line     line
          :ns       ns
          :function function})))

   :cljs
   (defn- parse-stacktrace-entry [e]
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
   :default
   (throw (ex-info "Platform not supported" {:ex-type :unexpected-platform})))

#?(:clj
   (defn get-call-context []
     (let [stacktrace  (-> (Throwable.) .getStackTrace)]
       (some
        parse-stacktrace-entry
        stacktrace)))

   :cljs
   (defn get-call-context []
     (let [stacktrace  (string/split (-> (js/Error.) .-stack) #"\n")]
       (some
        parse-stacktrace-entry
        stacktrace)))

   :default
   (throw (ex-info "Platform not supported" {:ex-type :unexpected-platform})))

#?(:clj
   (defn get-full-call-context []
     (let [stacktrace  (-> (Throwable.) .getStackTrace)]
       (map
        parse-stacktrace-entry
        stacktrace)))

   :cljs
   (defn get-full-call-context []
     (let [stacktrace  (string/split (-> (js/Error.) .-stack) #"\n")]
       (map
        parse-stacktrace-entry
        stacktrace)))

   :default
   (throw (ex-info "Platform not supported" {:ex-type :unexpected-platform})))

(def DEMUNGE_VEC
  [["_COLON_"       ":"]
   ["_PLUS_"        "+"]
   ["_GT_"          ">"]
   ["_LT_"          "<"]
   ["_EQ_"          "="]
   ["_TILDE_"       "~"]
   ["_BANG_"        "!"]
   ["_CIRCA_"       "@"]
   ["_SHARP_"       "#"]
   ["_SINGLEQUOTE_" "'"]
   ["_DOUBLEQUOTE_" "\\\""]
   ["_PERCENT_"     "%"]
   ["_CARET_"       "^"]
   ["_AMPERSAND_"   "&"]
   ["_STAR_"        "*"]
   ["_BAR_"         "|"]
   ["_LBRACE_"      "{"]
   ["_RBRACE_"      "}"]
   ["_LBRACK_"      "["]
   ["_RBRACK_"      "]"]
   ["_SLASH_"       "/"]
   ["_BSLASH_"      "\\\\"]
   ["_QMARK_"       "?"]
   ["_"             "-"]])

(defn deserialize-stacktrace-fn-name [fn-name]
  (loop [in DEMUNGE_VEC
         out fn-name]
    (let [[s r :as curr-in] (first in)
          rest-in (rest in)]
      (if curr-in
        (recur rest-in (string/replace out s r))
        out))))
