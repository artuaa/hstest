(ns hs.routes
  (:require [bidi.bidi :as bidi]
            [bidi.ring :refer [make-handler]]
            [hs.patient :as patient]
            [clojure.java.io :as io]))

(defn index-handler [req] {:status 200
                           :headers {"Content-type" "text/html"}
                           :body (-> "public/index.html" io/resource slurp)})

(defn not-found [req] {:status 404 :body "page not found"})

(defn health [req] {:status 200 :body "ok"})


(def routes
  ["" {"/" {:get #'index-handler}
                     "/health" {:get #'health}
                     "/api" {"/patients" {:get #'patient/get-many}
                             ["/patient/" :id] {:post #'patient/create-handler
                                               :put #'patient/update-handler
                                               :delete #'patient/delete-handler}}
                     true #'not-found}])

(defn root-handler [{req :request :as ctx}]
    (let [{:keys [uri]} req
          {:keys [handler route-params]} (bidi/match-route* routes uri req)]
      (handler (assoc-in ctx [:request :params] route-params))))

(comment
  (def req {:request-method :post :uri "/api/patient/1"})
    (bidi/match-route* routes (:uri req) req)
  (root-handler req))
