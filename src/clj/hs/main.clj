(ns hs.main
  (:require [ring.adapter.jetty :as adapter]
            [hs.app :refer [app]])
  (:gen-class))

(def server (atom nil))

(defn start []
  (let [port 8080]
    (println (str "Server started: " port))
    (reset! atom (adapter/run-jetty #'app {:port port}))))

(defn stop [] (@server))

(defn -main [& args]
  (start))
