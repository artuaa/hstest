(ns main-test
  (:require [hs.main :as sut]
            [clojure.test :refer :all]
            [matcho.core :as m]
            ;; [funtest :as ft]
            ))

(deftest test-app
  (def ctx (sut/start {:db {:dbname "db_test"}}))
  (def handler (:handler @ctx))

  (defn match [req exp]
    (let [resp (handler req)]
      (m/match resp exp)
      resp))

  (match
   {:request-method :get
    :uri "/health"}
   {:status 200})

  (def p {:name "piu piu"
          :address "Mosocw"
          :birthdate "2012-11-11"
          :policy "1234123412341234"
          :gender "male"})

  (match {:request-method :post
          :uri "/api/patient"
          :body {:patient p}}
         {:status 201 :body {:id int?}})

  ;; (ft/onreq {:status 200}
  ;;           {:request-method :get :uri "/api/patients" :body {:patient p}})

  ;; (ft/onreq {:status 404}
  ;;           {:request-method :delete :uri "/api/patient/0"})

  ;; (ft/onreq {:status 400}
  ;;           {:request-method :delete :uri "/api/patient/badid"})

  )
