(ns ^:figwheel-hooks hs.front.core
  (:require
   [reagent.dom :as r.dom]
   [hs.front.spec]
   [hs.front.state :as state]
   [reagent.session :as session]
   [cljs-time.core :as time]
   [cljs-time.format :as tf]
   [accountant.core :as accountant]
   [bidi.bidi :as bidi]
   [clojure.spec.alpha :as s]
   [reagent.core :as r]
   [reagent.ratom :as ratom]
   [hs.front.command :refer [dispatch!]]
   [hs.front.state :refer [app-state]]
   [hs.front.handlers]
   [clojure.string :as str]))

(def select-patients
  (ratom/make-reaction
   #(-> @(ratom/cursor app-state [:patients])
        vals vec)))
;; (defn select-patient [id]
;;   (ratom/make-reaction
;;    #(@(ratom/cursor app-state [:patients id]))))

(def app-routes
  ["/" {"" :index
        "patient" {"" :create
                   ["/" :id] :update}}])

(defmulti page-contents identity)

(defn format-date [date]
  (when date (tf/unparse
              (tf/formatter "YYYY-MM-dd")
              (time/to-default-time-zone date))))

(defmethod page-contents :index []
  (dispatch! :patients/get-many)
  (fn [] (let [headcol (fn [child]
                         [:th {:scope "col"
                               :class "px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"}
                          child])
               rowcol (fn [child]
                        [:td {:class "truncate max-w-xs px-6 py-4"} child])]
           [:div {:class "flex flex-col"}
            [:div {:class "-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8"}
             [:div {:class "py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8"}
              [:div {:class "shadow overflow-hidden border-b border-gray-200 sm:rounded-lg"}
               [:table {:class "min-w-full divide-y divide-gray-200"}
                [:thead {:class "bg-gray-50"}
                 [:tr
                  [headcol "Name"]
                  [headcol "Gender"]
                  [headcol "Birthdate"]
                  [headcol "Address"]
                  [headcol "Policy number"]
                  [headcol ""]
                  [headcol "Actions"]]]
                [:tbody {:class "bg-white divide-y divide-gray-200"}
                 (for [item @select-patients]
                   ^{:key (:id item)}
                   [:tr
                    [rowcol (:name item)]
                    [rowcol (:gender item)]
                    [rowcol (format-date (:birthdate item))]
                    [rowcol (:address item)]
                    [rowcol (:policy item)]
                    [rowcol
                     [:a {:href (bidi/path-for app-routes :update :id (:id item))
                          :class "text-indigo-600 hover:text-indigo-900"}
                      "Edit"]]
                    [rowcol
                     [:button {:on-click #(dispatch! :patients/delete (:id item))
                               :class "text-red-600 hover:text-red-900"}
                      "Delete"]]])]]]]]])))

(defn input [& {:keys [on-change value label type error min max]}]
  [:div {:class "flex flex-col mb-2"} [:label label]
   [:input {:class (str "p-1 rounded-md border "
                        (if (nil? error) "border-gray-400" "border-red-400"))
            :min min
            :max max
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
                          (js/console.log (clj->js exp))
                          (reset! errors (get-errors exp))))
        format-date (fn [date]
                      (tf/unparse (tf/formatter "YYYY-MM-dd") date))
        maxdate (format-date (time/now))
        mindate (format-date (time/minus (time/now) (time/years 100)))]
    (fn []
      [:form {:class "flex flex-col w-screen max-w-xl"}
       [input :error (:name @errors)
        :label "Name"
        :on-change (change :name)
        :value (:name @patient)]
       [:label "Gender"]
       [:select {:class "mb-2 p-1 rounded-md border border-gray-400"
                 :placeholder "Gender"
                 :on-change
                 (change :gender)
                 :value (:gender @patient)}
        [:option {:value "male"} "Male"]
        [:option {:value "female"} "Female"]]
       [input
        :error (:birthdate @errors)
        :label "Birthday"
        :type "date"
        :min mindate
        :max maxdate
        :on-change (change :birthdate)
        :value (:birthdate @patient)]
       [input
        :error (:address @errors)
        :label "Address"
        :on-change (change :address)
        :value (:address @patient)]
       [input :error (:policy @errors)
        :label "Policy"
        :on-change (change :policy)
        :value (:policy @patient)]
       [:button
        {:class "bg-yellow-200 rounded-md border w-1/2 mt-6 self-center"
         :on-click
         (fn [e]
           (.preventDefault e)
           (validate)
           (js/console.log (clj->js @errors))
           (when (empty? @errors)
             (submit-fn (s/conform :hs.front.spec/patient @patient))))}
        "Save"]])))

(defmethod page-contents :create []
  (let [initial {:name ""
                 :birthdate nil
                 :gender "male"
                 :policy ""
                 :address ""}
        on-submit
        (fn [v]
          (dispatch! :patients/create v)
          (accountant/navigate! "/"))]
    [form initial on-submit]))

(defn wrap-preload [page]
  (let [id (-> (session/get :route) :route-params :id js/parseInt)
        patient (-> @app-state :patients (get id))]
    (if (nil? patient)
      (do (dispatch! :patients/get-one id)
          "patient not found :(")
      [page patient])))

(defn update-page [patient]
  (let [initial (s/unform :hs.front.spec/patient patient)
        on-submit
        (fn [v]
          (dispatch! :patients/update v)
          (accountant/navigate! "/"))]
    [form initial on-submit]))

(defmethod page-contents :update []
  (-> update-page wrap-preload))

(defmethod page-contents :default [] [:div "page not found"])

(defn app []
  (fn [] (let [route (-> (session/get :route) :current-page)]
           [:div [:div {:class "my-4"}
                  [:a {:class "hover:underline mr-4"
                       :href (bidi/path-for app-routes :index)} "HOME"]
                  [:a {:class "hover:underline"
                       :href (bidi/path-for app-routes :create)} "CREATE"]]
            ^{:key route} [page-contents route]])))

(defn mount []
  (r.dom/render [app] (js/document.getElementById "root")))

(defn ^:after-load re-render []
  (mount))

(defonce start-up (do (mount) true))

(defn ^:export init! []
  (accountant/configure-navigation!
   {:nav-handler (fn [path]
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
  (def p {:name "hello"
          :gender ""
          :address ""
          :policy "123412341234123"
          :birthdate "2012-13-13"})
  (s/unform :hs.front.spec/patient p)
  (map :path (::s/problems expn))

  (second (::s/problems expn))
  (get-errors expn)

  (session/get :route)
  (bidi/match-route app-routes "/patient/123")
  (bidi/path-for app-routes :update :id 13)
  (->> @state/state :patients (filter #(= (:id %) 360)) first)
  (def d (js/Date. (.now js/Date)))
  @app-state
  (type d)
  (format-date d))
