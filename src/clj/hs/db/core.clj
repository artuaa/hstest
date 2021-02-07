(ns hs.db.core)

;; (def db  {:dbtype "postgresql"
;;           :dbname "db_dev"
;;           :host "localhost"
;;           :user "postgres"
;;           :password "mysecretpassword"
;;           :port 5432})

(defn connection [ctx]
  (let [cfg (get-in ctx [:config :db])]
    {:dbtype "postgresql"
      :dbname (:dbname cfg)
      :host "localhost"
      :user "postgres"
      :password "mysecretpassword"
      :port 5432}))
