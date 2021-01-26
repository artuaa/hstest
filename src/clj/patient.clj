
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
(defn update-handler [req] (println req)
  (let [id (get-in req [:params :id])
        entity (dissoc (get-in req [:body :patient]) :id)
        upd? (-> (j/update! db :patients entity ["id = ?" id])
                 first count zero? not)]
                   {:status 200 :body {:updated upd?}}))

(defn- gen-uuid [] (.toString (java.util.UUID/randomUUID)))

(defn create-handler [req] (println req)
  (let [id (get-in req [:params :id])
        entity (assoc (get-in req [:body :patient]) :id (gen-uuid))
        crt? (not nil? (j/insert! db :patients entity))]
                   {:status 200 :body {:created crt?}}))


(defn delete-handler [req] (let [count (first (j/delete! db :patients ["id = ?" (get-in req [:params :id])]))]
                             (if (zero? count) {:status 404} {:status 200})))

(comment
  (j/insert! db :patients {:id "1" :name "hello"}))
