(ns hs.front.events
  (:require
            [hs.front.errors :as err]
            [clojure.walk :as walk]
            ;; [camel-snake-kebab.core :as csk]
            ;; [camel-snake-kebab.extras :as cske]
            [clojure.string :as str]))
;;events
(def listeners (atom []))

(defn emit!
  ([type] (emit! type nil))
  ([type payload]
   (println type)
   (doseq [listen-fn @listeners]
     (listen-fn type payload))))

(defn register-listener! [listen-fn]
  (swap! listeners conj listen-fn))

(def base-url "http://localhost:8080")

(defn- is-json-response [res]
  (if-let [ct (.get (.-headers res) "Content-Type")]
    (str/starts-with? ct "application/json")
    false))

;;api
(defn do-request!
  ([method path cb] (do-request! method path nil cb))
  ([method path body cb]
   (let [serialized-body (when body
                           (->> body
                                ;; (cske/transform-keys csk/->camelCaseString)
                                (clj->js)
                                (js/JSON.stringify)))]
     (-> (js/fetch (str base-url path)
                   (cond-> {:method (name method)
                            ;; :headers {"Authorization" (str "Bearer " js/API_TOKEN)}
                            ;; :credentials "include"
                            }
                     (some? body)
                     (->
                      (assoc :body serialized-body)
                      (update :headers merge {"content-type" "application/json"}))
                     :always
                     clj->js))
         (.then (fn [res]
                  (if (.-ok res)
                    (when (is-json-response res)
                      (.json res))
                    (throw (ex-info "API Request Failed"
                                    {:status-code (.-status res)
                                     :status (.-statusText res)}
                                    :api-failure)))))
         (.then #(->> %
                      (js->clj)
                      ;; (cske/transform-keys csk/->kebab-case-keyword)
                      (walk/keywordize-keys)
                      (err/ok)
                      (cb)))
         (.catch #(do (println %)
                      (cb (err/error %))))))))

(defn- display-error [err]
  (emit! :notification/added
         {:type :error
          :text (str "API Error: " (ex-message err))}))

(defn- with-error-handling [f]
  (fn [res]
    (->> res
         (err/map f)
         (err/unwrap-or display-error))))

(defn- get-patients! []
  (do-request! :get "/api/patients"
               (with-error-handling #(emit! :patients/received (:patients %)))))

(defn- get-patient! [id]
  (do-request! :get (str "/api/patient/" id)
               (with-error-handling #(emit! :patient/received (:patient %)))))

(defn- create-patient! [patient]
  (do-request! :post "/api/patient" {:patient patient}
               (with-error-handling #(do (dispatch! :patients/get-many)
                                         (emit! :patient/created %)))))

(defn- update-patient! [patient]
  (do-request! :put (str "/api/patient/" (:id patient)) {:patient patient}
               (with-error-handling #(do
                                       (dispatch! :patients/get-many)
                                       (emit! :patient/updated patient)))))

(defn- delete-patient! [id]
  (do-request! :delete (str "/api/patient/" id)
               (with-error-handling #(emit! :patient/deleted id))))


(defn dispatch!
  ([command] (dispatch! command nil))
  ([command payload]
   (println command payload)
   (js/setTimeout
    (fn []
      (case command
        ;; :route/navigate (handle-navigate! payload)

        (:app/init :patients/get-many) (get-patients!)
        :patients/get-one (get-patient! payload)
        :patients/create (create-patient! payload)
        :patients/update (update-patient! payload)
        :patients/delete (delete-patient! payload)

        ;; :notification/add (handle-add-notification! payload)
        ;; :notification/remove (handle-remove-notification! payload)

        (js/console.error (str "Error: unhandled command: " command))))
    0)))

(comment
  (def resp (atom nil))
  (-> (js/fetch "http://localhost:8080/api/patients")
      (.then #(reset! resp %)))
  @resp
  (is-json-response @resp)
  (str/starts-with? (.get (.-headers @resp) "Content-Type") "application/json")
  (get-patients!))
