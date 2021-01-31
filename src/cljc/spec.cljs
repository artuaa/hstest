(ns spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::->date
  (s/conformer
   (fn [value]
     (let [parsed (js/Date.parse value)]
       (if (js/isNaN parsed) :clojure.spec.alpha/invalid
           (js/Date. parsed))))))

(s/def :patient/birthdate (s/and
                           ::ne-string
                           ::->date))

(s/def ::patient
  (s/keys :req-un [:patient/name
                   :patient/birthdate
                   :patient/address
                   :patient/gender
                   :patient/policy]
          :opt-un [:patient/id]))

(defn validate [val] (let [result (s/conform ::patient val)]
                       (if (= result :clojure.spec.alpha/invalid)
                         [false (s/explain-data ::patient val)]
                         [true result])))

(defn confrom [p] (s/conform ::patient p))

(comment
  (s/conform ::->date "2000")
  (s/conform ::->date "inv")
  (s/valid? :patient/birthdate "inv"))
