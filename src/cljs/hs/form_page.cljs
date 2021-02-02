(ns hs.form-page
  (:require
   [clojure.spec.alpha :as s]
   [hs.state :as st]
   [reagent.core :as r]))

(st/create-patient)

(defn- validate [p]
  (let [result (s/conform :hs.spec/patient p)]
    (js/console.log result)
    (if (= result :cljs.spec.alpha/invalid)
      [false (s/explain-data :hs.spec/patient p)]
      [true result])))

(defn- create [p](let [{ok :ok} (st/create-patient)]
                   (when (not ok) (js/alert "Create error"))))

(defn- submit [p]
  (js/console.log "submit")
  (let [[ok result] (validate p)]
    (if ok (create p)
        (do (js/console.log (clj->js result))
          (js/alert "Data is invalid")))))

(defn- form [p]
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
                             (submit @patient))} "Save"]])))

(defn page [] (form nil))
;; (defn page [] (fn [] (if (empty? @st/patients)
;;            [:span "Loading"]
;;            (form (first (filter #(= (:id %) nil) @st/patients))))))
