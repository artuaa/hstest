(ns ^:figwheel-hooks hs.main
  (:require
   [hs.form-page :as form]
   [reagent.dom :as r.dom]
   [reagent.core :as r]))

(def route (r/atom {:path "patient" :id "f64d1806-35e0-42b6-b7ff-25f37576229c"}))

(defn app []
    (fn [] form/page))

(defn mount []
  (r.dom/render [app] (js/document.getElementById "root")))

(defn ^:after-load re-render []
  (mount))

(defonce start-up (do (mount) true))
