(ns app-test
  (:require [app :refer [app]]
            [clojure.test :refer :all]))

(deftest test-health
  (let [request {:request-method :get :uri "/health"}
        response (app request)
        {:keys [status body]} response] (is (= 200 status))))

(deftest test-create-patient
  (let [data {:name "hello"}
        request {:request-method :post :uri "/api/patient" :body data}
        response (app request)
        {:keys [status body]} response] (is (= 400 status))))