
(ns patient
  (:require [db.core :refer [db]]
            [clojure.spec.alpha :as s]
            [spec :as spec]
            [clojure.java.jdbc :as j]))

(def spec-errors {:patient "patient is invalid"
                  :patient/name "name is invalid"
                  :patient/gender "gender is invalid"
                  :patient/birthdate "birthdate is invalid"
                  :patient/address "address is invalid"
                  :patient/policy "policy is invalid"})
(defn get-message
  [problem]
  (let [{:keys [via]} problem]
    (->> via first
         (keep spec-errors) first)))

(defn- get-patients [] (let [query "select * from patients"]
                         (j/query db query)))

(defn get-many [req] {:status 200
                      :body {:patients (get-patients)}})

(defn get-one-handler [req] (let [query ["select * from patients where id = ?" (get-in req [:params :id])]
                                  patient (first
                                           (j/query db query))]
                              (if (nil? patient) {:status 404} {:status 200 :body {:patient patient}})))

(defn update-handler [req] (println req)
  (let [id (get-in req [:params :id])
        entity (dissoc (get-in req [:body :patient]) :id)
        upd? (-> (j/update! db :patients entity ["id = ?" id])
                 first zero? not)]
    {:status 200 :body {:updated upd?}}))

(defn create-handler [req] (println req)
  (let [entity (-> req :body :patient)
        [ok? result] (spec/validate entity)]
    (if ok? (do (j/insert! db :patients result)
                {:status 200})  {:status 400})))

(defn delete-handler [req] (let [count (first (j/delete! db :patients ["id = ?" (get-in req [:params :id])]))]
                             (if (zero? count) {:status 404} {:status 200})))
(comment
  (j/insert! db :patients {:id "hello2" :birthdate (s/conform :hs/patient/birthdate "1234") :name "hello"}))
