(ns hs.back.core-test
  (:require [hs.back.core :as sut]
            [clojure.test :refer :all]
            [matcho.core :as m]
            [clojure.java.jdbc :as jdbc]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]))

(def ctx (sut/start {:db {:dbname  "db_test"}
                     :handler {:naked true}}))

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

(deftest test-crud
  (def create-resp (match {:request-method :post
                           :uri "/api/patient"
                           :body {:patient p}}
                     {:status 201 :body {:id int?}}))

  (def created-id (-> create-resp :body :id))

  ;; update
  (match
   {:request-method :put
    :uri (format "/api/patient/0" created-id)
    :body {:patient p}}
    {:status 404})

  (match
   {:request-method :put
    :uri (format "/api/patient/badid" created-id)
    :body {:patient p}}
    {:status 400})

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
   {:request-method :get
    :uri (format "/api/patient/badid" created-id)
    :body {:patient p}}
    {:status 400})

  (match
   {:request-method :delete
    :uri (format "/api/patient/%s" created-id)}
    {:status 200})

  (match
   {:request-method :delete
    :uri (format "/api/patient/badid" created-id)
    :body {:patient p}}
    {:status 400})

  (match
   {:request-method :delete
    :uri (format "/api/patient/%s" created-id)}
    {:status 404})

  (match
   {:request-method :get
    :uri (format "/api/patients" created-id)}
    {:status 200 :body {:patients vector?}}))
