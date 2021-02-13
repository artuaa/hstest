(ns hs.front.command
  (:require [hs.front.api :as api]))

(defn dispatch!
  ([command] (dispatch! command nil))
  ([command payload]
   (js/setTimeout
    (fn []
      (case command
        ;; :route/navigate (handle-navigate! payload)

        :patients/get-many (api/get-patients!)
        :patients/get-one (api/get-patient! payload)
        :patients/create (api/create-patient! payload)
        :patients/update (api/update-patient! payload)
        :patients/delete (api/delete-patient! payload)

        ;; :notification/add (handle-add-notification! payload)
        ;; :notification/remove (handle-remove-notification! payload)

        ;; :patient/received (patient-received)
        :patients/received (patients-received)
        ;; :patient/deleted (patient-deleted)

        (js/console.error (str "Error: unhandled command: " command))))
    0)))
