(ns hs.back.ssr
  (:require [selmer.parser :as sp]
            [hs.back.patient :as repo]))

(defn home-page [ctx]
  {:status 200
   :headers {"content-type" "text/html; charset=utf-8"}
   :body (sp/render-file "table.html" {:title "Patients" :patients
                                       (repo/get-patients ctx)
                                       })})
