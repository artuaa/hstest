
(ns hs.app
  (:require [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.logger :refer [wrap-with-logger]]
            [hs.db.core :refer [db]]
            [hs.routes :as r]))

(def ctx* (atom {:db db}))

(def app
  "Main Ring handler for the application"
  (-> (fn [req] (r/root-handler (assoc @ctx* :request req)))
      (wrap-cors :access-control-allow-origin [#"http://localhost:9500"] :access-control-allow-methods [:get :put :post :delete])
      (wrap-resource "public")
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-with-logger))

(comment
  (app {:request-method :put :uri "/api/patient/123"}))
