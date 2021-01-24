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
  (GET "/patients" [] #'patient/get-many)
  (GET "/patient/:id" [] #'patient/get-one-handler)
  (POST "/patient" [] #'patient/create-handler)
  (PUT "/patient/:id" [] #'patient/update-handler)
  (DELETE "/patient/:id" [] #'patient/delete-handler)
  (route/not-found "Not found"))
