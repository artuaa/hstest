(ns hs.front.state
  (:require [hs.front.events :refer [register-listener!]]
            [reagent.core :as r]
            [hs.front.events :as events]))

(def app-state (r/atom {:patients {}}))

(def handlers (atom {}))

(defn register-handler! [event-type handler-fn]
  (swap! handlers assoc event-type handler-fn))


(events/register-listener!
 (fn [type payload]
   (println "register")
   (when-let [handler-fn (get @handlers type)]
     (swap! app-state #(handler-fn  % payload)))))
