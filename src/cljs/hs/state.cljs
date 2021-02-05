(ns hs.state
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs-http.client :as http]
   [reagent.core :as r]
   [hs.spec]
   [clojure.spec.alpha :as s]
   [cljs.core.async :refer [<!]]))

(defn get-url [path] (str "http://localhost:8080" path))

(def state (r/atom {:patients []}))

(defn get-patients [] (go (let [{:keys [status body]} (<! (http/get (get-url "/api/patients") {:with-credentials? false}))]
                            (swap! state assoc :patients (-> body :patients))
                            {:ok (< (:status status) 300)})))
(comment
  @state
  (swap! state assoc :patients [123])
  (get-patients)
  )

;; (defn delete-patient [id] (when (js/confirm "delete patient?")
;;                             (http/delete (str (get-url "/api/patient/") id)
;;                               (fn [_] (swap! patients (fn [old] (remove (fn [v] (= (:id v) id)) old)))))))

;; (defn create-patient [patient] (http/post (get-url "/api/patient") {:patient patient} (fn [](js/alert "patient saved")
;;                                                                                           (open-main))))

(defn update-patient [patient] (go (let [resp (<! (http/put (get-url "/api/patient") {:body {:patient patient}}))]
                                     (js/alert "patient updated"))))

;; (defn update-patient [patient] (http/put (str (get-url "/api/patient/") (:id patient)) {:patient patient} (fn [](js/alert "patient updated")
                                                                                                            ;; (open-main))))

(defn delete-patient [id] {})

(defn create-patient
  [patient] (go (let [[status body]
                      (<! (http/post (get-url "/api/patient")
                                     {:json-params {:patient patient}
                                      :with-credentials? false}))]
                  {:ok (< status 300)})))
