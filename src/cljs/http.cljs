(ns hs.http
  (:require [clojure.walk :as cw]))


(defn GET [url  on-success] (-> (js/fetch url #js {"method" "get"})
                                (.then  #(.json %))
                                (.catch #(on-success nil))
                                (.then #(on-success (cw/keywordize-keys (js->clj %))))))

(defn DELETE [url  on-success] (-> (js/fetch url #js {"method" "delete"})
                                   (.then  #(.json %))
                                   (.catch #(on-success nil))
                                   (.then #(on-success (cw/keywordize-keys (js->clj %))))))
