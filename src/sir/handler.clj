(ns sir.handler
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:use compojure.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [org.httpkit.client :as http]
            [ring.adapter.jetty :as jetty]
            [clojure.string :as str]
            [sir.goog :as goog]
            [sir.bart :as bart]
            [sir.muni :as muni]
            [environ.core :refer [env]]
            [compojure.route :as route]))


; TODO
; remove routes without times after they are back from the second api request
(defn fetch-agency-data [trip]
  (let [trip-or-trips (cond
                        (= (:agency trip) "bart") (bart/fetch trip)
                        (= (:agency trip) "muni") (muni/fetch trip)
                        :else trip)]
    (if (vector? trip-or-trips)
      trip-or-trips
      (conj [] trip-or-trips))))

(defn same-trip? [tripA tripB]
  (if (= (:eolStationName tripA) (:eolStationName tripB))
    (= (:originStationName tripA) (:originStationName tripB))
    nil))

(defn trip-in-set? [trip trips]
  (some #(same-trip? % trip) trips))

(defn make-uniq [results]
  (reduce
    (fn [collector trip]
      (if (trip-in-set? trip collector)
        collector
        (conj collector trip)))
    []
    results))

(defn parse-results
  [{:keys [routes status]}]
  (let [trips
          (make-uniq
            (apply concat
              (map #(goog/parse-route %) routes)))]
    (let [all-timed-trips
            (reduce
              (fn [collector trip]
                (let [timed-trips (fetch-agency-data trip)]
                  (into collector timed-trips)))
              []
              trips)]
      all-timed-trips)))

(defn fetch-trips
  [body params]
  (let [url
        (if (contains? params :startLat)
          (goog/build-url-params params)
          (goog/build-url body))]
    (let [{:keys [status headers body error]} @(http/get url)]
      (if error
        {:status 400
         :body "fail"}
        {:status 200
          :headers {"Content-Type" "application/json"}
          :body (generate-string (parse-results (parse-string body true)))}))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (POST "/" {body :body params :params} (fetch-trips body params))
  ; (POST "/" {body :body params :params} (fetch-trips body params))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body {:keywords? true})
      (middleware/wrap-json-response)))

(defn -main []
  (let [port (Integer. (or (env :port) 3000))]
    (jetty/run-jetty app {:port port})))