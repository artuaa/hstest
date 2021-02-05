(ns ^:figwheel-hooks hs.main
  (:require
   [hs.form-page :as form]
   [reagent.dom :as r.dom]
   [hs.state :as state]
   [reagent.session :as session]
   [accountant.core :as accountant]
   [bidi.bidi :as bidi]
   [reagent.core :as r]))

(comment
  (session/get :route)
  (bidi/match-route app-routes :index))

(def app-routes
  ["/" {"" :index
        "patient/" {"" :form
                    [:id] :form}}])
(defmulti page-contents identity)

(state/get-patients)

(defmethod page-contents :index []
    [:div [:h1 "Patients"]
     [:table
      [:thead [:tr [:th "Name"] [:th "Gender"] [:th "Birthday"] [:th "Address"] [:th "Policy"] [:th "Actions"]]]
      [:tbody (map (fn [item]
                     [:tr {:key (:id item)}
                      [:td [:a {:href "heloo"} (:name item)]]
                      [:td (:gender item)]
                      [:td (:birthday item)]
                      [:td (:address item)]
                      [:td (:policy item)]
                      [:td [:a {:href (bidi/path-for app-routes :form)} "edit"]
                       [:button {:on-click #(state/delete-patient (:id item))} "Delete"]]])
                   (:patients @state/state))]]])

(defmethod page-contents :form [] [:div "form"])

(defmethod page-contents :default [] [:div "hell    o"])

(defn app []
  (fn [] (let [route (-> (session/get :route) :current-page)]
          [:div [:div [:a {:href (bidi/path-for app-routes :index)} "HOME"]
                 [:a {:href (bidi/path-for app-routes :form)} "CREATE"]]
           (page-contents route)])))

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
