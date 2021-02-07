(ns main-test
  (:require [hs.main :as sut]
            [clojure.test :refer :all]
            [matcho.core :as m]
            [clojure.java.jdbc :as jdbc]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]))

;; (defn init-db [db] (jdbc/execute! db "drop table if exists patients;"))
                    ;; (->> (io/resource "sql/create.sql")
                    ;;      io/file
                    ;;      slurp
                    ;;      (jdbc/execute! db))

(def ctx (sut/start {:db {:dbname  "db_test"}}))
  ;; (init-db (:db @ctx))
(def handler (:handler @ctx))

(defn match [req exp]
  (let [resp (handler req)]
    (m/match resp exp)
    resp))

(def p {:name "piu piu"
        :address "Mosocw"
        :birthdate "2012-11-10"
        :policy "1234123412341234"
        :gender "male"})

(deftest test-cud
  (match
   {:request-method :get
    :uri "/health"}
    {:status 200})

  (def create-resp (match {:request-method :post
                           :uri "/api/patient"
                           :body {:patient p}}
                     {:status 201 :body {:id int?}}))

  (def created-id (-> create-resp :body :id))

  (match
   {:request-method :put
    :uri (format "/api/patient/%s" created-id)
    :body {:patient p}}
    {:status 200})

  (match
   {:request-method :get
    :uri (format "/api/patient/%s" created-id)}
    {:status 200 :body {:patient map?}})

  (match
   {:request-method :delete
    :uri (format "/api/patient/%s" created-id)}
    {:status 200}))

(deftest test-getall
  (handler {:request-method :post
            :uri "/api/patient"
            :body {:patient p}})
  (handler {:request-method :post
            :uri "/api/patient"
            :body {:patient p}})
  (handler {:request-method :post
            :uri "/api/patient"
            :body {:patient p}})

  (match
   {:request-method :get
    :uri "/api/patients"}
    {:status 200 :body {:patients vector?}}))
