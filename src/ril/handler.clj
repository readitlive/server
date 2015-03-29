(ns ril.handler
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:use compojure.core
        cheshire.core
        ring.util.response
        org.httpkit.server)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.adapter.jetty :as jetty]
            [clojure.string :as str]
            [clojure.core :refer :all]
            [environ.core :refer [env]]
            [compojure.route :as route]))

(def clients (atom {}))

(defn posts
  [req]
  (with-channel req con
    (swap! clients assoc con true)
    (println con " connected")
    (on-close con (fn [status]
                   (swap! clients dissoc con)
                   (println con " disconnected. Status: " status)))))

(future (loop []
         (doseq [client @clients]
          (send! (key client) (generate-string
                               {:happiness (rand 10)})
                 false))
         (Thread/sleep 5000)
         (recur)))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/" [] posts)
  ; (POST "/" {body :body params :params} (fetch-trips body params))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body {:keywords? true})
      (wrap-cors :access-control-allow-origin #".+")
      (middleware/wrap-json-response)))

(defn -main []
  (let [port (Integer. (or (env :port) 3000))]
    (jetty/run-jetty app {:port port})))


; example json handler

; (defn fetch-trips
;   [body params]
;   (if (valid-request? body params)
;     (let [req-data (normalize-request body params)]
;       (let [url (goog/build-url req-data)]
;         (let [data (if (cache/has? @goog-cache (cache-name req-data))
;                      (cache/lookup @goog-cache (cache-name req-data))
;                      (let [fresh-data @(http/get url)]
;                        (swap! goog-cache assoc (cache-name req-data) fresh-data)
;                        fresh-data))]
;           (let [{:keys [status headers body error]} data]
;             (if error
;               {:status 418
;                :body "fail"}
;               {:status 200
;                :headers {"Content-Type" "application/json"}
;                :body (generate-string (parse-results (parse-string body true)))})))))
;     {:status 400
;      :body "bad request"}))


;; (def goog-cache (atom (cache/ttl-cache-factory {} :ttl (* 1000 60 3))))
; (let [data (if (cache/has? @goog-cache (cache-name req-data))
;              (cache/lookup @goog-cache (cache-name req-data))
;              (let [fresh-data @(http/get url)]
;                (swap! goog-cache assoc (cache-name req-data) fresh-data)
;                fresh-data))]
