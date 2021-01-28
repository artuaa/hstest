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
  
  ;; id VARCHAR(255) NOT NULL UNIQUE PRIMARY KEY,
  ;; name VARCHAR(255) NOT NULL,
  ;; birthDate DATE,
  ;; address VARCHAR(255),
  ;; gender VARCHAR(15),
  ;; policy VARCHAR(255)

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
                        (fn [val](= int-len) 16)))
(s/def ::patient
  (s/keys :req-un [:patient/name
                   :patient/birthdate
                   :patient/address
                   :patient/gender
                   :patient/policy]
          :opt-un [:patient/id]))

(def p {:id "hello"
    :name "Alex"
    :gender "male"
    :birthdate "2023"
    :address "Moscow, Red Square"
    :policy 2344})

(s/valid? ::patient p)

(def spec-errors
  {::->date "invalid date"})

(s/conform ::->date "2134-12-33sldklf")

(get-message {:via [::->date]})


(defn get-message
  [problem]
  (let [{:keys [via]} problem
        spec (last via)]
    (get spec-errors spec)))
