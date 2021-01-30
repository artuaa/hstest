(ns app-test
  (:require [app :refer [app]]
            [db.core :as conn]
            [clojure.java.jdbc :as jdbc]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [spec :as spec]
            [clojure.test :refer :all]))

(def db* (assoc conn/db :dbname "db_test"))

(defn create-patients-table [] ((jdbc/execute! db* "drop table if exists ")
                                (->> (io/resource "sql/create.sql")
                                     io/file
                                     slurp
                                     (jdbc/execute! db*))))

(defn recreate-patients-table []
  (let [query (-> (io/resource "sql/create.sql")
                  io/file slurp
                  (clojure.string/replace #"\n" " "))]
    (jdbc/execute! db* "drop table if exists patients")
    (jdbc/execute! db* query)))

(defn fix-insert-data [t] (let [patients (->> (io/resource "seeds/patients.edn")
                                              io/file
                                              slurp
                                              edn/read-string
                                              :data
                                              (map spec/confrom)
                                              (map #(dissoc % :id)))]
                            (recreate-patients-table)
                            (jdbc/insert-multi! db* :patients patients)
                            (t)))

(use-fixtures :each fix-insert-data)

(deftest test-health
  (let [request {:request-method :get :uri "/health"}
        response (app request)
        {:keys [status body]} response] (is (= 200 status))))

(def p {:name "piu piu"
        :address "Mosocw"
        :birthdate "2012-11-11"
        :policy "1234123412341234"
        :gender "male"})

(deftest test-create-patient
  (let [data p
        request {:request-method :post :uri "/api/patient" :body {:patient data}}
        response (app request)
        {:keys [status body]} response]
    (is (= 200 status))))

(comment
  (fix-insert-data)
  (jdbc/execute! db* "truncate patients cascade;")
  (jdbc/execute! db* "select count(*) from patients"))
