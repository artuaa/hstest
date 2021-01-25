(ns routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [patient :as patient]
            [clojure.java.io :as io]))


(defn index-handler [req] {:status 200
                           :headers {"Content-type" "text/html"}
                           :body (-> "public/index.html" io/resource slurp)})

(defroutes root-handler
  (GET "/" [] index-handler)
  (GET "/api/patients" [] #'patient/get-many)
  (GET "/api/patient/:id" [] #'patient/get-one-handler)
  (POST "/api/patient" [] #'patient/create-handler)
  (PUT "/api/patient/:id" [] #'patient/update-handler)
  (DELETE "/api/patient/:id" [] #'patient/delete-handler)
  (route/not-found "Not found"))
