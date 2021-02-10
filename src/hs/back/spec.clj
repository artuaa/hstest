(ns hs.back.spec
  (:require [clojure.spec.alpha :as s]
            [hs.shared.spec :as base]
            [clojure.instant :refer [read-instant-date]]))

(s/def ::->date
  (s/conformer
   (fn [value]
     (try
       (-> (read-instant-date value)
           (.getTime)
           (java.time.Instant/ofEpochMilli)
           (.atZone (java.time.ZoneId/of "UTC"))
           (.toLocalDateTime))
       (catch Exception e
         ::s/invalid)))))

(s/def :patient/birthdate (s/and
                           ::base/ne-string
                           ::->date))

(s/def ::patient
  (s/keys :req-un [:patient/name
                   :patient/birthdate
                   :patient/address
                   :patient/gender
                   :patient/policy]
          :opt-un [:patient/id]))

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
