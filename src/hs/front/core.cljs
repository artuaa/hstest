(ns ^:figwheel-hooks hs.front.core
  (:require
   [reagent.dom :as r.dom]
   [hs.front.spec]
   [hs.front.state :as state]
   [reagent.session :as session]
   [accountant.core :as accountant]
   [bidi.bidi :as bidi]
   [clojure.spec.alpha :as s]
   [reagent.core :as r]))

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

(defn input [& {:keys [on-change value label type error]}]
  [:div {:class "flex flex-col mb-2"} [:label label]
   [:input {:class (str "p-1 rounded-md border "
                        (if (nil? error) "border-gray-400" "border-red-400"))
            :placeholder label
            :type type
            :on-change on-change
            :value value}]
   [:span {:class "text-red-400"} error]])

(def error-messages {:name "Name required"
                     :gender "Gender required"
                     :address "Adress required"
                     :birthdate "Birthdate required"
                     :policy "Policy number must contain 16 characters"})

(defn get-errors [{problems ::s/problems}]
  (->> problems (map (comp first :path))
       (reduce #(assoc %1 %2 (get error-messages %2)) {})))

(defn form [initial submit-fn]
  (let [patient (r/atom initial)
        errors (r/atom {})
        change  (fn [key] (fn [input] (swap! patient assoc key (-> input .-target .-value))))
        validate (fn [] (let [exp (s/explain-data :hs.front.spec/patient @patient)]
                          (reset! errors (get-errors exp))))]
    (fn []
      [:form {:class "flex flex-col"}
       [input :error (:name @errors)  :label "Name" :on-change (change :name)  :value (:name @patient)]
       [:label "Gender"]
       [:select {:class "mb-2 p-1 rounded-md border border-gray-400" :placeholder "Gender" :on-change (change :gender) :value (:gender @patient)}
        [:option {:value "male"} "Male"]
        [:option {:value "female"} "Female"]]
       [input :error (:birthdate @errors) :label "Birthday" :type "date" :on-change (change :birthdate) :value (:birthdate @patient)]
       [input :error (:address @errors) :label "Address" :on-change (change :address) :value (:address @patient)]
       [input :error (:policy @errors) :label "Policy" :on-change (change :policy) :value (:policy @patient)]
       [:button {:class "bg-yellow-200 rounded-md border w-1/2 mt-6 self-center"
                 :on-click (fn [e]
                             (.preventDefault e)
                             (validate)
                             ;; (submit-fn @patient)
                             )}"Save"]])))

(defmethod page-contents :create []
  (let [initial {:name ""
                 :birthdate nil
                 :gender "male"
                 :policy ""
                 :address ""}]
    [form initial #()]))

(defn update-patient [v] (let [conformed (s/conform :hs.front.spec/patient v)]
                           (if (= conformed ::s/invalid)
                             (js/alert "Data is invalid")
                             (state/update-patient conformed))))

(defmethod page-contents :update []
  (let [id (-> (session/get :route) :route-params :id js/parseInt)
        initial (->> @state/state :patients (filter #(= (:id %) id)) first)]

    (js/console.log initial)
    [form initial update-patient]))

(defmethod page-contents :default [] [:div "page not found"])

(defn app []
  (fn [] (let [route (-> (session/get :route) :current-page)]
           [:div [:div [:a {:href (bidi/path-for app-routes :index)} "HOME"]
                  [:a {:href (bidi/path-for app-routes :create)} "CREATE"]]
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

(comment
  (def expn (s/explain-data
             :hs.front.spec/patient
             {:name "hello"
              :gender ""
              :address ""
              :policy "123412341234123"
              :birthdate "2012-13-13"}))

  (map :path (::s/problems expn))

  (def error-messages {:name "Name required"
                       :gender "Gender required"
                       :address "Adress required"
                       :birthdate "Birthdate required"
                       :policy "Policy number must contain 16 characters"})

  (second (::s/problems expn))
  (get-errors expn)

  (session/get :route)
  (bidi/match-route app-routes "/patient/123")
  (bidi/path-for app-routes :update :id 13)
  (->> @state/state :patients (filter #(= (:id %) 360)) first))
