(ns ril.handler
  ; (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:use [compojure.core :only [defroutes GET POST PUT DELETE context]]
        cheshire.core
        ring.util.response
        ring.middleware.cors)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.middleware.reload :as reload]
            [ring.adapter.jetty :as jetty]
            [clojure.string :as str]
            [clojure.core :refer :all]
            [environ.core :refer [env]]
            [org.httpkit.server :as ws]
            [compojure.route :as route]))

(def clients (atom {}))

(defn posts
  [req]
  (println "posts handler")
  (ws/with-channel req channel
   (swap! clients assoc channel true)
   (println channel " connected")
   (ws/on-close channel (fn [status]
                      (swap! clients dissoc channel)
                      (println channel " disconnected. Status: " status)))
    (if (:websocket? channel)
      (println "WebSocket channel")
      (println "HTTP channel"))
   (ws/on-receive channel (fn [data]
                         (ws/send! channel "hi!")))))

; (future (loop []
;          (doseq [client @clients]
;           (ws/send! (key client) (generate-string
;                                {:happiness (rand 10)})
;                  false))
;          (Thread/sleep 5000)
;          (recur)))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/ws" req (posts req))) ; websocket
  ; (POST "/" {body :body params :params} (fetch-trips body params))
  ; (route/files "/static/")  ; in public folder?
  ; (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      reload/wrap-reload
      (wrap-cors :access-control-allow-origin #".+")))
      ; (middleware/wrap-json-body {:keywords? true})
      ; (middleware/wrap-json-response)))

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
