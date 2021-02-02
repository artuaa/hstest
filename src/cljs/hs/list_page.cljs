(ns hs.list-page
  (:require [hs.state :as st]))

(defn page []
  (fn []
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
                      [:td [:button {:on-click #(swap! route assoc :path "patient" :id (:id item))} "Edit"]
                       [:button {:on-click #(delete-patient (:id item))} "Delete"]]])
                   @st/patients)]]]))
