(defproject hs "0.1.0-SNAPSHOT"
  :main main
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring "1.7.1"]
                 [ring/ring-json "0.4.0"]
                 [ring-logger "1.0.1"]
                 [compojure "1.6.1"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.5"]]
  :source-paths ["src"])
