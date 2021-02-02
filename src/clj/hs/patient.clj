(ns hs.patient
  (:require [hs.db.core :refer [db]]
            [hs.spec :as hss]
            [clojure.spec.alpha :as s]
            [clojure.data.json :as json]
            [clojure.java.jdbc :as j]))

(defn- parse-id [val]
  (try (Integer/parseInt val)
       (catch Exception e nil)))


(defn- get-patients [] (let [query "select * from patients"]
                         (j/query db query)))

(defn get-many [req] {:status 200
                      :body {:patients (get-patients)}})

(defn get-one-handler [req] (let [id (-> req :params :id Integer/parseInt)
                                  query ["select * from patients where id = ?" id]
                                  patient (first
                                           (j/query db query))]
                              (if (nil? patient) {:status 404} {:status 200 :body {:patient patient}})))

(defn update-handler [req] (println req)
  (if-let [id (-> req :params :id parse-id)]
    (let [entity (dissoc (get-in req [:body :patient]) :id)
          upd? (-> (j/update! db :patients entity ["id = ?" id])
                   first zero? not)]
      {:status 200 :body {:updated upd?}}
      {:status 400})))

(defn create-handler [req] (println req)
  (let [entity (-> req :body :patient)
        result (s/conform ::hss/patient entity)
        ok (not= result ::s/invalid)]
    (print result)
    (if ok (do (j/insert! db :patients result)
               {:status 200})  {:status 400
                                :body (json/write-str (s/explain-data ::hss/patient entity))})))

(defn delete-handler [req] (if-let [id (-> req :params :id parse-id)] (let [count (first (j/delete! db :patients ["id = ?" id]))]
                                                                        (if (zero? count) {:status 404} {:status 200})) {:status 400}))
(comment
  (j/insert! db :patients {:id "hello2" :birthdate (s/conform :hs/birthdate "1234") :name "hello"}))
