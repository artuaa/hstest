(ns hs.front.api
  (:require [hs.front.events :refer [emit!]]
            [hs.front.errors :as err]
            [clojure.walk :as walk]
            ;; [camel-snake-kebab.core :as csk]
            ;; [camel-snake-kebab.extras :as cske]
            ))

(def base-url "http://localhost:8080")

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
                    (when (= 200 (.-status res))
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
         (.catch #(cb (err/error %)))))))

(defn- display-error [err]
  (emit! :notification/added
         {:type :error
          :text (str "API Error: " (ex-message err))}))

(defn- with-error-handling [f]
  (fn [res]
    (->> res
         (err/map f)
         (err/unwrap-or display-error))))

(defn get-patients! []
  (do-request! :get "/api/patients"
               (with-error-handling #(emit! :patients/received (:patients %)))))

(defn get-patient! [id]
  (do-request! :get (str "/api/patient/" id)
               (with-error-handling #(emit! :patient/received (:patient %)))))

(defn create-patient! [patient]
  (do-request! :post "/api/patient" {:patient patient}
               (with-error-handling #(emit! :patient/created %))))

(defn update-patient! [patient]
  (do-request! :put (str "/api/patient/" (:id patient)) {:patient patient}
               (with-error-handling #(emit! :patient/updated patient))))

(defn delete-patient! [id]
  (do-request! :delete (str "/api/patient/" id) note
               (with-error-handling #(emit! :patient/deleted id))))

(comment
  (get-patients!))
;; (defn do-search! [text]
;;   (println "TODO: Submit search" text))
