(ns hs.front.spec
  (:require [clojure.spec.alpha :as s]
            [cljs-time.format :as tfmt]
            [cljs-time.core :as time]
            [hs.shared.spec :as base]))

(s/def ::->date
  (s/conformer
   (fn [value]
     (let [parsed (js/Date.parse value)]
       (if (js/isNaN parsed) :clojure.spec.alpha/invalid
           (js/Date. parsed))))
   (fn [value]
     (when value (tfmt/unparse
                  (tfmt/formatter "YYYY-MM-dd")
                  (time/to-default-time-zone value))))))

(s/def :patient/birthdate
  (s/and
   ::base/ne-string
   ::->date))

(s/def ::patient
  (s/keys
   :req-un
   [:patient/name
    :patient/birthdate
    :patient/address
    :patient/gender
    :patient/policy]
   :opt-un
   [:patient/id]))

(comment
  (s/conform ::->date "2000")
  (s/conform ::->date "inv")
  (s/unform ::patient {:gender "hello"})
  (s/valid? :patient/birthdate "inv")
  (time/unparse (tfmt/formatter "YYYY-MM-dd")))
