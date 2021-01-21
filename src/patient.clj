
(ns patient
  (:require [db.core :refer [db]]
            [clojure.java.jdbc :as j]))

(defn- get-patients [] (let [query "select * from patients"]
                         (j/query db query)))

(defn get-many [req] {:status 200
                      :body {:patients (get-patients)}})

(defn get-one-handler [req] (let [query ["select * from patients where id = ?" (get-in req [:params :id])]
                                  patient (first
                                           (j/query db query))]
                              (if (nil? patient) {:status 404} {:status 200 :body {:patient patient}})))
;; TODO: validate errors 
;; TODO: check not found
(defn update-handler [req] (let [upd (j/update! db :patients (get-in req [:body :patient]) ["id = ?" (get-in req [:params :id])])]
                             {:status 200}))

(defn create-handler [req] (let [entity (first (j/insert! db :patients
                                                          (assoc (get-in req [:body :patient]) :id  (.toString (java.util.UUID/randomUUID)))))]
                             {:status 200 :body {:id (:id entity)}}))

(defn delete-handler [req] (let [count (first (j/delete! db :patients ["id = ?" (get-in req [:params :id])]))]
                             (if (zero? count) {:status 404} {:status 200})))

(comment
  (j/insert! db :patients {:id "123" :name "hello"}))