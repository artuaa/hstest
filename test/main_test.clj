(ns main-test
  (:require [hs.main :as sut]
            [clojure.test :refer :all]
            [matcho.core :as m]
            ;; [funtest :as ft]
            ))

(deftest test-app
  (def ctx (sut/start {:db {:dbname "db_test"}}))
  (def handler (:handler @ctx))
  (def req {:request-method :get :uri "/health"})

  (m/match (handler req) {:status 200})

  (handler {:request-method :get :uri "/api/patients"})

  (def p {:name "piu piu"
          :address "Mosocw"
          :birthdate "2012-11-11"
          :policy "1234123412341234"
          :gender "male"})

  (def req {:request-method :post :uri "/api/patient" :body {:patient p}})
  (m/match (handler req) {:status 201 :body {"id" int?}})

  ;; (ft/onreq {:status 200}
  ;;           {:request-method :get :uri "/api/patients" :body {:patient p}})

  ;; (ft/onreq {:status 404}
  ;;           {:request-method :delete :uri "/api/patient/0"})

  ;; (ft/onreq {:status 400}
  ;;           {:request-method :delete :uri "/api/patient/badid"})

  )
