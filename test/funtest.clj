(ns funtest
  (:require  [clojure.test :as t]
             [app :refer [app]]
             [db.core :as conn]
             [clojure.java.jdbc :as jdbc]
             [clojure.edn :as edn]
             [matcho.core :as m]
             [clojure.java.io :as io]
             [spec :as spec]))

(defn onreq [exp req]
  (m/assert exp (app req)))

(comment
  (onreq {:status 200} {:request-method :get :uri "/health"}))

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

(comment
  (fix-insert-data)
  (jdbc/execute! db* "truncate patients cascade;")
  (jdbc/execute! db* "select count(*) from patients"))
