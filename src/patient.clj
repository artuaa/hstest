
(ns patient
  (:require [db.core :refer [connection]]
            [clojure.java.jdbc :as j]))



(defn patients [])

(defn get-patients [] (let [query "select * from patients"]
                        (j/query connection query)))

(defn get-patients-handler [req] {:status 200
                                  :body {:patients (get-patients)}})

(defn get-patient-handler [req] (let [patient (first (filter #(= (:id %)
                                                                 (get-in req [:params :id])) patients))]
                                  (if (nil? patient) {:status 404 :body {:error "Patient not found"}}
                                      {:status 200
                                       :body {:patient patient}})))
                ;; TODO: validate errors 
                 ;; TODO: check not found
(defn update-handler [req] (let [upd (j/update! connection :patients (get-in req [:body :patient]) ["id = ?" (get-in req [:params :id])])]
                             {:status 200}))

(defn create-handler [req] (let [upd (j/insert! connection :patients
                                                (assoc (get-in req [:body :patient]) :id  (.toString (java.util.UUID/randomUUID))))]
                             {:status 200}))

