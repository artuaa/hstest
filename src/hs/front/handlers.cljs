(ns hs.front.handlers
  (:require [hs.front.state :refer [register-handler!]]
            [clojure.spec.alpha :as s]))


(defn patients-received [state patients]
  (->> patients
       (map #(s/conform :hs.front.spec/patient %))
       (reduce #(assoc %1 (:id %2) %2) {})
       (assoc state :patients)))

(defn patient-received [state patient]
  (let [conformed (s/conform :hs.front.spec/patient patient)]
    (assoc-in state [:patients (:id conformed)] conformed)))

(defn patient-deleted [state id]
  (update-in state [:patients] dissoc id))


(register-handler! :patients/received patients-received)

(register-handler! :patient/received patient-received)

(register-handler! :patient/deleted patient-deleted)


(comment
 (def state {:patients {1 {} 2 {}}})
 (patient-deleted state 1))
