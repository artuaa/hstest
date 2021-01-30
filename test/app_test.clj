(ns app-test
  (:require [app :refer [app]]
            [db.core :refer [db]]
            [clojure.java.jdbc :as jdbc]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [spec :as spec]
            [clojure.test :refer :all]))

(defn fix-insert-data [] (let [patients (->> (io/resource "seeds/patients.edn")
                                            io/file
                                            slurp
                                            edn/read-string
                                            :data
                                            (map spec/confrom)
                                            (map #(dissoc % :id)))]
                           (jdbc/execute! db "truncate patients cascade;")
                           (jdbc/insert-multi! db :patients patients)))

(deftest test-health
  (let [request {:request-method :get :uri "/health"}
        response (app request)
        {:keys [status body]} response] (is (= 200 status))))

(deftest test-create-patient
  (let [data {:name "hello"}
        request {:request-method :post :uri "/api/patient" :body data}
        response (app request)
        {:keys [status body]} response] (is (= 400 status))))

(comment
  (fix-insert-data)
  (jdbc/execute! db "truncate patients cascade;")
  (jdbc/execute! db "select count(*) from patients"))
