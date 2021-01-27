(ns spec
  (:require [clj-time.format :as f]
            [clojure.spec.alpha :as s]))

(s/def ::->date
  (s/conformer
   (fn [value]
   (try
     (f/parse (f/formatter "YYYY-mm-dd") value)
     (catch Exception e
       ::s/invalid)))))

(def spec-errors
  {::->date "invalid date"})

(s/conform ::->date "2134-12-33----")
