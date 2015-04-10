(defproject sir "0.1.0-SNAPSHOT"
  :description "Socket server for Read it Live"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [http-kit "2.1.18"]
                 [ring/ring-devel "1.3.2"]
                 [ring-cors "0.1.6"]
                 [compojure "1.3.2"]
                 [environ "1.0.0"]
                 [javax.servlet/servlet-api "2.5"]
                 [cheshire "4.0.3"]]
  :plugins [[lein-ring "0.9.3"]]
  :ring {:handler ril.test/app
         :auto-reload? true}
  :uberjar-name "read-it-live.jar"
  :profiles
   {:uberjar {:aot :all}
   :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})

; (defproject sir "0.1.0-SNAPSHOT"
;   :description "Socket server for Read it Live"
;   :url "http://example.com/FIXME"
;   :min-lein-version "2.0.0"
;   :dependencies [[org.clojure/clojure "1.6.0"]
;                  [ring/ring-json "0.3.1"]
;                  [ring/ring-jetty-adapter "1.3.2"]
;                  [http-kit "2.1.18"]
;                  [ring/ring-devel "1.3.2"]
;                  [ring-cors "0.1.6"]
;                  [compojure "1.3.2"]
;                  [environ "1.0.0"]
;                  [javax.servlet/servlet-api "2.5"]
;                  [cheshire "4.0.3"]]
;   :plugins [[lein-ring "0.9.3"]]
;   :ring {:handler ril.handler/app
;          :auto-reload? true}
;   :uberjar-name "read-it-live.jar"
;   :profiles
;    {:uberjar {:aot :all}
;    :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
;                         [ring-mock "0.1.5"]]}})



                        ; :dependencies [[org.clojure/clojure "1.6.0"]
                        ;                [compojure "1.3.2"]
                        ;                [ring/ring-json "0.3.1"]
                        ;                [ring/ring-jetty-adapter "1.3.2"]
                        ;                [jumblerg/ring.middleware.cors "1.0.1"]
                        ;                [c3p0/c3p0 "0.9.1.2"]
                        ;                [javax.servlet/servlet-api "2.5"]
                        ;                [org.clojure/core.cache "0.6.4"]
                        ;                [cheshire "4.0.3"]]
