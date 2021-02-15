(ns hs.back.db.core)

(defn connection [ctx]
  (let [cfg (get-in ctx [:config :db])]
    {:dbtype "postgresql"
     :dbname (:dbname cfg)
     :host "localhost"
     :user "postgres"
     :password "mysecretpassword"
     :port 5432}))
