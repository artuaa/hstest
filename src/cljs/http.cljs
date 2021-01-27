(ns hs.http
  (:require [clojure.walk :as cw]))

(defn- to-body [data] (.stringify js/JSON (clj->js data)))

(def headers #js {"Content-type" "application/json"})

(defn GET [url  on-success] (-> (js/fetch (js/encodeURI url) #js{"method" "get"})
                                (.then  #(.json %))
                                (.catch #(on-success nil))
                                (.then #(on-success (cw/keywordize-keys (js->clj %))))))

(defn DELETE [url  on-success] (-> (js/fetch (js/encodeURI url) #js{"method" "delete"})
                                   (.then  #(.json %))
                                   (.catch #(on-success nil))
                                   (.then #(on-success (cw/keywordize-keys (js->clj %))))))

(defn POST [url data on-success] (-> (js/fetch (js/encodeURI url) #js{"method" "post" "headers" headers "body" (to-body data)})
                                     (.then  #(.json %))
                                     (.catch #(on-success nil))
                                     (.then #(on-success (cw/keywordize-keys (js->clj %))))))

(defn PUT [url data on-success] (-> (js/fetch (js/encodeURI url) #js{"method" "put" "headers" headers "body" (to-body data)})
                                    (.then  #(.json %))
                                    (.catch #(on-success nil))
                                    (.then #(on-success (cw/keywordize-keys (js->clj %))))))
