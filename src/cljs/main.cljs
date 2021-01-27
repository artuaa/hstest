(ns ^:figwheel-hooks hs.main
  (:require
   [reagent.dom :as r.dom]
   [reagent.core :as r]
   [hs.http :as http]))

(def route (r/atom {:path "patient" :id "f64d1806-35e0-42b6-b7ff-25f37576229c"}))

(defn open-main []
  (reset! route {:path ""}))

(defn get-url [path] (str "http://localhost:8080" path))


(def patients (r/atom []))

(defn get-patients [] (http/GET (get-url "/api/patients") #(reset! patients (:patients %))))


(get-patients)

(defn delete-patient [id] (when (js/confirm "Delete patient?")
                            (http/DELETE (str (get-url "/api/patient/") id)
                              (fn [_] (swap! patients (fn [old] (remove (fn [v] (= (:id v) id)) old)))))))

(defn create-patient [patient] (http/POST (get-url "/api/patient") {:patient patient} (fn [](js/alert "Patient saved")
                                                                                          (open-main))))

(defn update-patient [patient] (http/PUT (str (get-url "/api/patient/") (:id patient)) {:patient patient} (fn [](js/alert "Patient updated")
                                                                                                            (open-main))))


(defn patient-table []
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
                   @patients)]]]))


(defn patient-form [p]
  (let [patient (r/atom p)
        change  (fn [key] (fn [input] (swap! patient assoc key (-> input .-target .-value))))]
    (fn []
      (if (nil? patient) [:span "loading"]
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
           [:button {:on-click #(do (.preventDefault %)
                                    (update-patient @patient))} "Save"]]))))

(defn patient-page []
  (fn [] (if (empty? @patients) [:span "Loading"] (patient-form (first (filter #(= (:id %) (:id @route)) @patients))))))

(defn app []
  (let [path (:path @route)]
    (fn [](cond
          (= path "patient") patient-page
           :else patient-table))))

(defn mount []
  (r.dom/render [app] (js/document.getElementById "root")))


(defn ^:after-load re-render []
  (mount))


(defonce start-up (do (mount) true))

(comment
  (deref patients)
  (deref route)
         (update-patient {:id "1c0ba9ac-fcc5-49ef-9279-92dec722f3ce"
                          :name "My update"
                          :birthdate nil
                          :address "Adler, Mira 13"
                          :gender "male"
                          :policy "number"
                          })
         (create-patient {:name "Alex Alex"
                          :birthdate nil
                          :address "Adler, Mira 13"
                          :gender "male"
                          :policy "number"
                          })
         )
