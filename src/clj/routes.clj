(ns routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [patient :as patient]))


(defroutes root-handler
  (GET "/patients" [] #'patient/get-many)
  (GET "/patient/:id" [] #'patient/get-one-handler)
  (POST "/patient" [] #'patient/create-handler)
  (PUT "/patient/:id" [] #'patient/update-handler)
  (DELETE "/patient/:id" [] #'patient/delete-handler)
  (route/not-found "Not found"))
