;; (ns main
;;   (:require [ring.adapter.jetty :refer [run-jetty]]
;;             [app :refer [app]])
;;   (:gen-class))

;; (defn -main
;;   [& args]
;;   (let [port 8080]
;;     (println (str "Run server on: " port))
;;     (run-jetty app {:port port})))

(ns main
  (:require [ring.adapter.jetty :as adapter]
            [app :refer [app]])
  (:gen-class))

(defn -main [& args]
  (let [port 8080]
    (println (str "Server started: " port))
    (adapter/run-jetty #'app {:port port})))
