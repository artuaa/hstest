(ns hs.front.state
  (:require [reagent.core :as r]
            [clojure.spec.alpha :as s]
            [hs.front.events :as events]))

(def app-state (r/atom {:patients {}}))

(def handlers (atom {}))

(defn register-handler! [event-type handler-fn]
  (swap! handlers assoc event-type handler-fn))

(events/register-listener!
 (fn [type payload]
   (when-let [handler-fn (get @handlers type)]
     (swap! app-state #(handler-fn  % payload)))))

;;handlers
(defn- patients-received [state patients]
  (->> patients
       (map #(s/conform :hs.front.spec/patient %))
       (reduce #(assoc %1 (:id %2) %2) {})
       (assoc state :patients)))

(defn- patient-received [state patient]
  (let [conformed (s/conform :hs.front.spec/patient patient)]
    (assoc-in state [:patients (:id conformed)] conformed)))

(defn- patient-deleted [state id]
  (update-in state [:patients] dissoc id))

(register-handler! :patients/received patients-received)

(register-handler! :patient/received patient-received)

(register-handler! :patient/deleted patient-deleted)

(register-handler! :patients/update (fn [state _]
                   (swap! state assoc-in [:page :form :pending] true)))

(register-handler! :patient/updated (fn [state _]
                   (swap! state assoc-in [:page :form :pending] false)))
