(ns hs.state
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs-http.client :as http]
   [reagent.core :as r]
   [hs.spec]
   [clojure.spec.alpha :as s]
   [cljs.core.async :refer [<!]]))

(defn get-url [path] (str "http://localhost:8080" path))
(defn ok? [status] (< status 300))

(def state (r/atom {:patients []}))

(defn get-patients [] (go (let [{:keys [status body]} (<! (http/get (get-url "/api/patients") {:with-credentials? false}))]
                            (swap! state assoc :patients (-> body :patients))
                            {:ok (< (:status status) 300)})))
(comment
  @state
  (swap! state assoc :patients [123])
  (swap! state update :patients (fn [coll] (remove #(= (:id %) 333) coll)))
  (get-patients))


;; (defn create-patient [patient] (http/post (get-url "/api/patient") {:patient patient} (fn [](js/alert "patient saved")
;;                                                                                           (open-main))))


(defn update-patient [patient]
  (go (let [{:keys [status body]} (<! (http/put (str (get-url "/api/patient/") (:id patient)) {:json-params {:patient patient} :with-credentials? false}))]
        (if (ok? status) (js/alert "Updated"))
        (js/alert "Update error"))))

;; (defn update-patient [patient] (http/put (str (get-url "/api/patient/") (:id patient)) {:patient patient} (fn [](js/alert "patient updated")
                                                                                                            ;; (open-main))))
(defn delete-patient [id]
  (go (when (js/confirm "delete patient?")
        (let [{:keys [status]} (<! (http/delete (str (get-url "/api/patient/") id) {:with-credentials? false}))]
          (swap! state update :patients (fn [coll] (remove #(= (:id %) id) coll)))))))

-;; (defn delete-patient [id] (when (js/confirm "delete patient?")
-;;                             (http/delete (str (get-url "/api/patient/") id)
-;;                               (fn [_] (swap! patients (fn [old] (remove (fn [v] (= (:id v) id)) old)))))))
(defn create-patient
  [patient] (go (let [[status body]
                      (<! (http/post (get-url "/api/patient")
                                     {:json-params {:patient patient}
                                      :with-credentials? false}))]
                  {:ok (< status 300)})))
