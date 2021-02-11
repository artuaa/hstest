(ns hs.back.core
  (:require
   [bidi.bidi :as bidi]
   [bidi.ring :refer [make-handler]]
   [hs.back.patient :as patient]
   [clojure.java.io :as io]
   [ring.middleware.json :refer [wrap-json-body
                                 wrap-json-response]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.logger :refer [wrap-with-logger]]
   [hs.back.spec]
   [hs.back.db.core :as dbcore]
   [hs.back.server.core :as server]
   [ring.util.response :as rr]
   [clojure.java.jdbc :as jdbc])
  (:gen-class))

(defn index-handler [req]
  (-> (rr/resource-response "index.html" {:root "public"})
      (rr/content-type "text/html; charset=utf-8")))

(defn not-found [req] {:status 404 :body "page not found"})

(defn health [req] {:status 200 :body "ok"})

(def routes
  ["/" {"" {:get #'index-handler}
        "patient" {:get #'index-handler}
        ["patient/" :id] {:get #'index-handler}
        "api" {"health" {:get #'health}
               "/patients" {:get #'patient/get-many}
               "/patient" {:post #'patient/create-handler}
               ["/patient/" :id] {:get #'patient/get-one-handler
                                  :put #'patient/update-handler
                                  :delete #'patient/delete-handler}}
        true #'not-found}])

(defn root-handler [{req :request :as ctx}]
  (let [{:keys [uri]} req
        {:keys [handler route-params]} (bidi/match-route* routes uri req)]
    (handler (assoc-in ctx [:request :params] route-params))))

(defn app [ctx]
  (-> (fn [req] (root-handler (assoc ctx :request req)))
      (wrap-cors :access-control-allow-origin [#"http://localhost:9500"] :access-control-allow-methods [:get :put :post :delete])
      (wrap-resource "public")
      wrap-json-response
      (wrap-json-body {:keywords? true})
      wrap-with-logger))

(def config {:server {:port 8080}
             :db {:dbname "db_dev"}})

(defn start [config]
  (let [ctx (atom {:config config})
        db (dbcore/connection @ctx)
        _ (swap! ctx assoc :db db)
        handler (app @ctx)
        _ (swap! ctx assoc :handler handler)
        server (when (:server config) (server/start {:port (get-in config [:server :port])} handler))
        _ (swap! ctx assoc :server server)] ctx))

(defn stop [ctx]
  (when-let [server (:server ctx)] (.stop server)))

(defn -main [& args]
  (start config))

(comment
  (def ctx (start config))
  (stop @ctx)
  (jdbc/execute! (:db @ctx) "delete from patients;")
  (def req {:request-method :get :uri "/"})
  (bidi/match-route* routes (:uri req) req)
  (root-handler req))
