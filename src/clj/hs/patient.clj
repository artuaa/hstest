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

(defn get-one-handler [req]
   (let [id (-> req :params :id Integer/parseInt)
                                  query ["select * from patients where id = ?" id]
                                  patient (first
                                           (j/query db query))]
                              (if (nil? patient) {:status 404} {:status 200 :body {:patient patient}})))
(defn wrap-id [handler]
  (fn [req]
    (if-let [id (-> req :params :id parse-id)]
      (handler (assoc-in req [:params :id] id))
      {:status 400 :body {:error_message "id is invalid"}})))

(defn wrap-patient [handler]
  (fn [req] (let [entity (-> req :body :patient)
                  conformed (s/conform :hs.spec/patient (dissoc entity :id))]
              (if (not= conformed ::s/invalid)
                (handler (assoc-in req [:body :patient] conformed))
                {:status 400
                 :body {:error_message "patient is invalid"
                        :error (s/explain-data :hs.spec/patient entity)}}))))

(defn- update-patient [req]
  (let [id (-> req :params :id)
        patient (-> req :body :patient)
        updated (-> (j/update! db :patients patient ["id = ?" id])
                    first zero? not)]
    (if (zero? updated)
      {:status 404
       :error_message "patient not found"}
      {:status 200})))

(def update-handler (-> update-patient wrap-patient wrap-id))

(comment
  (update-handler {:params {:id "13"}})
  (def patient {:name "hello"
                :gender "female"
                :address "hlelo"
                :policy "1234123412341234"
                :birthdate "2012-01-01"})
  (update-handler {:params {:id "13"} :body {:patient patient}}))

(defn create-patient [req]
  (let [patient (-> req :body :patient)]
    (j/insert! db :patients patient)
    {:status 200}))

(def create-handler (-> create-patient wrap-patient))

(defn delete-patient [req]
  (let [id (-> req :params :id)
        deleted (->(j/delete! db :patients ["id = ?" id]) first zero? not)]
      (if deleted {:status 200}
          {:status 404
           :body {:error_message "patient not found"}})))

(def delete-handler (-> delete-patient wrap-id))

(comment
  (j/insert! db :patients {:id "hello2" :birthdate (s/conform :hs/birthdate "1234") :name "hello"}))
