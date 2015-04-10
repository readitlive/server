(ns ril.test
  (:use [compojure.route :only [files not-found]]
        [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server
        ring.middleware.cors)
  (:require [ring.middleware.reload :as reload]))

(defn handler [req]
  (with-channel req channel
    (println channel)
    (:on-close channel (fn [status]
                        (println "channel closed")))
    (if (:websocket? channel)
      (println "WebSocket channel")
      (println "HTTP channel"))
    (:on-receive channel (fn [data]
                          (send! channel data)))))

(defroutes all-routes
  ; (GET "/" [] handler)
  (GET "/ws" [] handler)     ;; websocket
  (files "/static/") ;; static file url prefix /static, in `public` folder
  (not-found "<p>Page not found.</p>")) ;; all other, return 404


(def app
  (-> (site #'all-routes)
      reload/wrap-reload
      (wrap-cors :access-control-allow-origin #".+")))

(run-server app {:port 3000})

; (ns ril.test
;   (:use [compojure.core :only (defroutes GET)]
;         ring.util.response
;         ring.middleware.cors
;         org.httpkit.server)
;   (:require [compojure.route :as route]
;             [compojure.handler :as handler]
;             [ring.middleware.reload :as reload]
;             [cheshire.core :refer :all]))
;
; (def clients (atom {}))
;
; (defn ws
;   [req]
;   (with-channel req con
;     (swap! clients assoc con true)
;     (println con " connected")
;     (on-close con (fn [status]
;                     (swap! clients dissoc con)
;                     (println con " disconnected. status: " status)))))
;
; (future (loop []
;           (doseq [client @clients]
;             (send! (key client) (generate-string
;                                  {:happiness (rand 10)})
;                    false))
;           (Thread/sleep 5000)
;           (recur)))
;
; (defroutes routes
;   (GET "/happiness" [] ws))
;
; (def application (-> (handler/site routes)
;                      reload/wrap-reload
;                      (wrap-cors
;                       :access-control-allow-origin #".+")))
;
; (defn -main [& args]
;   (let [port (Integer/parseInt
;                (or (System/getenv "PORT") "8080"))]
;     (run-server application {:port port :join? false})))
