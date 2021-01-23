(ns ^:figwheel-hooks hs.main
  (:require
   [reagent.dom :as r.dom]))


(def patients [{:id 1
                :name "Alex"
                :gender "male"
                :birthday "12234"
                :address "Moscow, Red Square"
                :policy 12341234}
               {:id 2
                :name "Alex"
                :gender "male"
                :birthday "12234"
                :address "Moscow, Red Square"
                :policy 12341234}
               {:id 3
                :name "Alex"
                :gender "male"
                :birthday "12234"
                :address "Moscow, Red Square"
                :policy 12341234}
               {:id 4
                :name "Alex"
                :gender "male"
                :birthday "12234"
                :address "Moscow, Red Square"
                :policy 12341234}
               {:id 5
                :name "!!!!!Ron don don"
                :gender "male"
                :birthday "12234"
                :address "Moscow, Red Square"
                :policy 12341234}]
  )


(defn home-page []
  (fn []
    [:table
     [:thead [:tr [:th "Name"] [:th "Gender"] [:th "Birthday"] [:th "Address"] [:th "Policy"]]]
     [:tbody (map (fn [item]
                    [:tr {:key (:id item)} [:td [:a {:href "heloo"} (:name item)]] [:td (:gender item)] [:td (:birthday item)] [:td (:address item)] [:td (:policy item)]])
                  patients)]]))
 
;; (defn patient-page []
;;   (fn []
;;     (let [routing-data (session/get :route)
;;           item (get-in routing-data [:route-params :id])]
;;       [:form.form
;;        [:label {:for "Name"} "Name"]
;;        [:input {:id "Name" :placeholder "Name"}]
;;        [:label {:for "Gender"} "Gender"]
;;        [:select {:id "Gender" :placeholder "Gender"}
;;         [:option {:value "male"} "Male"]
;;         [:option {:value "female"} "Female"]]
;;        [:label {:for "Birthday"} "Birthday"]
;;        [:input {:id "Birthday" :placeholder "Birthday" :type "date"}]
;;        [:label {:for "Address"} "Address"]
;;        [:input {:id "Address" :placeholder "Address"}]
;;        [:label {:for "Policy"} "Policy"]
;;        [:input {:id "Policy" :placeholder "Policy"}]])))



(defn app []
  (home-page)
  )

(js/console.log "hello")

(defn mount []
  (r.dom/render [app] (js/document.getElementById "root")))


(defn ^:after-load re-render []
  (mount))


(defonce start-up (do (mount) true))
