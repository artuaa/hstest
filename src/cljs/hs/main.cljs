(ns ^:figwheel-hooks hs.main
  (:require
   [hs.form-page :as form]
   [reagent.dom :as r.dom]
   [reagent.session :as session]
   [hs.list-page :as table]
   [accountant.core :as accountant]
   [bidi.bidi :as bidi]
   [reagent.core :as r]))

(def app-routes
  ["/" {"" :index
        "patient/" {"" :form
                    [:id] :form}}])

(def handlers {:index (fn [] [:div "hello"])
               :form form/page})
(defn render-route [key] (if-let [m (get handlers key)]
                           (m)
                           [:div "page not found"]))
(comment
  (session/get :route)
  (bidi/match-route app-routes :index))

(defn app []
  (fn [] (let [route (-> (session/get :route) :current-page)]
           [:div (render-route route)])))

(defn mount []
  (r.dom/render [app] (js/document.getElementById "root")))

(defn ^:after-load re-render []
  (mount))

(defonce start-up (do (mount) true))

(defn ^:export init! []
  (accountant/configure-navigation!
   {:nav-handler (fn
                   [path]
                   (let [match (bidi/match-route app-routes path)
                         current-page (:handler match)
                         route-params (:route-params match)]
                     (session/put! :route {:current-page current-page
                                           :route-params route-params})))
    :path-exists? (fn [path]
                    (boolean (bidi/match-route app-routes path)))})
  (accountant/dispatch-current!))

(init!)
