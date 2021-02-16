(ns hs.front.effects
  (:require
   [hs.front.errors :as err]
   [clojure.walk :as walk]
            ;; [camel-snake-kebab.core :as csk]
            ;; [camel-snake-kebab.extras :as cske]
   [hs.front.events :refer [register-listener! emit!]]
   [clojure.string :as str]))

;;; API
(def base-url "http://localhost:8080")

(defn- is-json-response [res]
  (if-let [ct (.get (.-headers res) "Content-Type")]
    (str/starts-with? ct "application/json")
    false))

(defn do-request!
  ([method path cb] (do-request! method path nil cb))
  ([method path body cb]
   (let [serialized-body (when body
                           (->> body
                                ;; (cske/transform-keys csk/->camelCaseString)
                                (clj->js)
                                (js/JSON.stringify)))]
     (-> (js/fetch (str base-url path)
                   (cond-> {:method (name method)}
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
  (emit! :notification/show
         {:type :error
          :text (str "Api request failed")}))

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
               (with-error-handling #(do (emit! :patient/created %)
                                         (emit! :patients/get)))))

(defn- update-patient! [patient]
  (do-request! :put (str "/api/patient/" (:id patient)) {:patient patient}
               (with-error-handling #(do (emit! :patient/updated patient)
                                         (emit! :patients/get)))))

(defn- delete-patient! [id]
  (do-request! :delete (str "/api/patient/" id)
               (with-error-handling #(emit! :patient/deleted id))))

(defn- show-notification! [{text :text}]
  (js/alert text))

;;; EFFECTS
(def effects (atom {}))

(register-listener!
 (fn [type payload]
   (when-let [handler-fn (get @effects type)]
     (js/setTimeout (fn [] (handler-fn payload)) 0))))

(defn register-effect! [event-type handler-fn]
  (swap! effects assoc event-type handler-fn))

(register-effect! :app/init get-patients!)
(register-effect! :patient/get get-patient!)
(register-effect! :patients/get get-patients!)
(register-effect! :patient/create create-patient!)
(register-effect! :patient/update update-patient!)
(register-effect! :patient/delete delete-patient!)
;; (register-effect! :notification/show show-notification!)
