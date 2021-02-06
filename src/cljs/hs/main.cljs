(ns ^:figwheel-hooks hs.main
  (:require
   [reagent.dom :as r.dom]
   [hs.state :as state]
   [reagent.session :as session]
   [accountant.core :as accountant]
   [bidi.bidi :as bidi]
   [reagent.core :as r]))

(comment
  (session/get :route)
  (bidi/match-route app-routes "/patient/123")
  (bidi/path-for app-routes :update :id 13))

(def app-routes
  ["/" {"" :index
        "patient" {"" :create
                    ["/" :id] :update}}])

(defmulti page-contents identity)


(defmethod page-contents :index []
  (state/get-patients)
   (fn [] [:div [:h1 "Patients"]
            [:table
             [:thead [:tr [:th "Name"] [:th "Gender"] [:th "Birthday"] [:th "Address"] [:th "Policy"] [:th "Actions"]]]
             [:tbody (map (fn [item]
                            [:tr {:key (:id item)}
                             [:td [:a {:href "heloo"} (:name item)]]
                             [:td (:gender item)]
                             [:td (:birthday item)]
                             [:td (:address item)]
                             [:td (:policy item)]
                             [:td [:a {:href (bidi/path-for app-routes :update :id (:id item))} "edit"]
                              [:button {:on-click #(state/delete-patient (:id item))} "Delete"]]])
                          (:patients @state/state))]]]))

(defmethod page-contents :update []
  (let [initial {:name ""
                 :birthdate nil
                 :gender "male"
                 :policy ""
                 :address ""}
        patient (r/atom initial)
        change  (fn [key] (fn [input] (swap! patient assoc key (-> input .-target .-value))))]
    (fn []
      [:form.form
       [:label {:for "Name!"} "Name" (:name patient)]
       [:input {:id "Name" :placeholder "Name" :on-change (change :name)  :value (:name @patient)}]
       [:label {:for "Gender"} "Gender"]
       [:select {:id "Gender" :placeholder "Gender" :on-change (change :gender) :value (:gender @patient)}
        [:option {:value "male"} "Male"]
        [:option {:value "female"} "Female"]]
       [:label {:for "Birthday"} "Birthday"]
       [:input {:id "Birthday" :placeholder "Birthday" :type "date" :on-change (change :birthdate) :value (:birthdate @patient)}]
       [:label {:for "Address"} "Address"]
       [:input {:id "Address" :placeholder "Address" :on-change (change :address) :value (:address @patient)}]
       [:label {:for "Policy"} "Policy"]
       [:input {:id "Policy" :placeholder "Policy" :on-change (change :policy) :value (:policy @patient)}]
       [:button {:on-click (fn [e]
                             (.preventDefault e)
                             ;; (submit @patient)
                             )} "Save"]])))

(defmethod page-contents :default [] [:div "hello"])

(defn app []
  (fn [] (let [route (-> (session/get :route) :current-page)]
          [:div [:div [:a {:href (bidi/path-for app-routes :index)} "HOME"]
                 [:a {:href (bidi/path-for app-routes :form)} "CREATE"]]
          ^{:key route} [page-contents route]])))

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
