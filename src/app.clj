
(ns app
  (:require [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [routes :as r]))

(def app
  "Main Ring handler for the application"

  (-> r/root-handler
      wrap-json-response
      (wrap-json-body {:keywords? true})))
