(ns hs.main
  (:require [ring.adapter.jetty :as adapter]
            [app :refer [app]])
  (:gen-class))

(defn -main [& args]
  (let [port 8080]
    (println (str "Server started: " port))
    (adapter/run-jetty #'app {:port port})))

(-main)
