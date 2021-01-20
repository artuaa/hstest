
(ns patient
  (:require [db.core :refer [connection]]
            [clojure.java.jdbc :as j]))

(defn patients [])

(defn get-patients [] (let [query "select * from patients"]
                        (j/query connection query)))

(defn get-patients-handler [req] {:status 200
                                  :body {:patients (get-patients)}})

(defn get-patient-by-id-handler [req] (let [patient (first (filter #(= (:id %)
                                                                       (get-in req [:params :id])) patients))]
                                        (if (nil? patient) {:status 404 :body {:error "Patient not found"}}
                                            {:status 200
                                             :body {:patient patient}})))

