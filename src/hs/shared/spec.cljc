(ns hs.shared.spec
  (:require [clojure.spec.alpha :as s]))


(s/def ::ne-string
  (s/and string? not-empty))

(s/def :patient/id int?)
(s/def :patient/name ::ne-string)
(s/def :patient/address ::ne-string)
(s/def :patient/gender (s/and
                        ::ne-string
                        (s/conformer
                         clojure.string/lower-case
                         identity)
                        (fn [val] (contains? #{"male" "female"} val))))

(s/def :patient/policy (s/and
                        ::ne-string
                        (fn [val] (= (count val) 16))))

(comment (def p {:id 1234
                 :name "Alex"
                 :gender "mALe"
                 :birthdate "2023"
                 :address "Moscow, Red Square"
                 :policy 1111111111111111})
         (validate p)
         (s/valid? :patient/policy "1234123412341234")
         (s/explain-data :hs/patient p)
         (s/conform :patient/birthdate "2933")
         (s/conform ::->date "2012-11-11"))
