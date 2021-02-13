
(ns hs.front.events)

(def listeners (atom []))

(defn emit!
  ([type] (emit! type nil))
  ([type payload]
   (doseq [listen-fn @listeners]
     (listen-fn type payload))))

(defn register-listener! [listen-fn]
  (swap! listeners conj listen-fn))

(defn hello [payload] (js/console.log payload))

(defn dispatch!
  ([command] (dispatch! command nil))
  ([command payload]
   (js/setTimeout
    (fn []
      (case command
        :hello (hello payload)
        (js/console.error (str "Error: unhandled command: " command))))
    0)))


(comment
  (register-listener!
   (fn [type payload]
     (println "Listener 1")
     (println "Type:" type "Payload:" payload)))

  (register-listener!
   (fn [type payload]
     (println "Listener 2")
     (println "Type:" type "Payload:" payload)))

  (register-listener! dispatch!)

  (emit! :foo {:name "cooper"}))
