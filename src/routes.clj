(ns routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [patient :as patient]))

(defn test-handler [req] {:status 200 :body {:author "Artur"}})

(defroutes root-handler
  (GET "/test" [] #'test-handler)
  (GET "/patients" [] #'patient/get-patients-handler)
  (GET "/patient/:id" [] #'patient/get-patient-by-id-handler)
  (route/not-found "Not found"))
