(ns spec
  (:require [clojure.spec.alpha :as s]
            [clojure.instant :refer [read-instant-date]]))

(s/def ::->date
  (s/conformer
   (fn [value]
     (try
       read-instant-date
       (catch Exception e
         ::s/invalid)))))

(defn int-len [val] (-> val
                        .toString
                        count))

(s/def ::ne-string
  (s/and string? not-empty))

(s/def :patient/id ::ne-string)
(s/def :patient/name ::ne-string)
(s/def :patient/birthdate (s/and
                           ::ne-string
                           ::->date))
(s/def :patient/address ::ne-string)
(s/def :patient/gender (s/and
                        ::ne-string
                        (s/conformer clojure.string/lower-case)
                        (fn [val] (contains? #{"male" "female"} val))))

(s/def :patient/policy (s/and
                        int?
                        (fn [val] (= (int-len val) 16))))
(s/def ::patient
  (s/keys :req-un [:patient/name
                   :patient/birthdate
                   :patient/address
                   :patient/gender
                   :patient/policy]
          :opt-un [:patient/id]))

(def spec-errors
  {::->date "invalid date"})

(defn get-message
  [problem]
  (let [{:keys [via]} problem
        spec (last via)]
    (get spec-errors spec)))

(comment (def p {:id "hello"
                 :name "Alex"
                 :gender "mALe"
                 :birthdate "2023"
                 :address "Moscow, Red Square"
                 :policy 1111111111111111})
         (s/explain-data ::patient p)
         (s/conform ::patient p))
