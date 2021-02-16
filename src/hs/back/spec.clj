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

(s/def :patient/birthdate
  (s/and
   ::base/ne-string
   ::->date))
(s/def :patient/created
  (s/and
   ::base/ne-string
   ::->date))
(s/def :patient/updated
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
   [:patient/id
    :patient/created
    :patient/updated]))
