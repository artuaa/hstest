(ns hs.front.state
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs-http.client :as http]
   [reagent.core :as r]
   [clojure.spec.alpha :as s]
   [cljs.core.async :refer [<!]]))

;; {
;;  :ok true
;;  :error "Some error"
;; }

(defn request! [req]
  (http/request
   (merge req {:url (str "http://localhost:8080" (:url req))
               :with-credentials? false})))

(defn show-error [message] (j/alert message))

(defn ok? [status] (< status 300))

(def app-state (r/atom {:patients {}}))

(defn patients-loaded [state patients]
  (->> patients
       (map #(s/conform :hs.front.spec/patient %))
       (reduce #(assoc %1 (:id %2) %2) {})
       (state assoc :patients)))

(defn patient-loaded [state patient]
  (let [conformed (s/conform :hs.front.spec/patient patient)]
    (state assoc (:id conformed) conformed)))

(defn patient-deleted [state id]
  (dissoc-in state [:patients id]))

(defn get-patients! []
  (go (let [{:keys [status body]} (<! (request! {:method :get :url "/api/patients"}))]
        (swap! state assoc :patients
               (->> body
                    :patients
                    (map #(s/conform :hs.front.spec/patient %))
                    (reduce #(assoc %1 (:id %2) %2) {})))
        {:ok (< (:status status) 300)})))

(defn update-patient [patient]
  (go (let [{:keys [status body]}
            (<! (http/put (str (get-url "/api/patient/") (:id patient))
                          {:json-params {:patient patient} :with-credentials? false}))]
        (if (ok? status) (js/alert "Updated"))
        (js/alert "Update error"))))

(defn delete-patient [id]
  (go (when (js/confirm "delete patient?")
        (let [{:keys [status]}
              (<! (http/delete (str (get-url "/api/patient/") id)
                               {:with-credentials? false}))]
          (swap! state update :patients (fn [coll] (remove #(= (:id %) id) coll)))))))

(defn create-patient
  [patient] (go (let [[status body]
                      (<! (http/post (get-url "/api/patient")
                                     {:json-params {:patient patient}
                                      :with-credentials? false}))]
                  {:ok (< status 300)})))

(comment
  @state
  (swap! state assoc :patients [123])
  (swap! state update :patients (fn [coll] (remove #(= (:id %) 333) coll)))

  (get-patients))
