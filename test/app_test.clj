(ns app-test
  (:require [hs.app :refer [app]]
            [clojure.test :refer :all]
            [funtest :as ft]))

(deftest test-app
  (ft/onreq {:status 200}
            {:request-method :get :uri "/health"})

  (def p {:name "piu piu"
          :address "Mosocw"
          :birthdate "2012-11-11"
          :policy "1234123412341234"
          :gender "male"})

  (ft/onreq {:status 201}
            {:request-method :post :uri "/api/patient" :body {:patient p}})

  (ft/onreq {:status 200}
            {:request-method :get :uri "/api/patients" :body {:patient p}})

  (ft/onreq {:status 404}
            {:request-method :delete :uri "/api/patient/0"})

  (ft/onreq {:status 400}
            {:request-method :delete :uri "/api/patient/badid"}))
