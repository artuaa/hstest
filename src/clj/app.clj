
(ns app
  (:require [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.logger :refer [wrap-with-logger]]
            [routes :as r]))

(def app
  "Main Ring handler for the application"
  (-> r/root-handler
      ;;(wrap-cors :access-control-allow-origin [#"http://localhost:9500"] :access-control-allow-methods [:get :put :post :delete])
      (wrap-resource "public")
      wrap-json-response
      wrap-with-logger
      (wrap-json-body {:keywords? true})))
