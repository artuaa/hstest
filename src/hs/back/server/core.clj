(ns hs.back.server.core
  (:require [ring.adapter.jetty :as adapter]))

(defn start [cfg handler]
  (adapter/run-jetty handler {:port (:port cfg) :join? false}))
