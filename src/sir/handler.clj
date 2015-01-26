(ns sir.handler
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:use compojure.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [org.httpkit.client :as http]
            [clojure.string :as str]
            [sir.goog :as goog]
            [sir.bart :as bart]
            [sir.muni :as muni]
            [compojure.route :as route]))


; TODO
; remove routes without times after they are back from the second api request
(defn fetch-agency-data [trip]
  (cond
    (= (:agency trip) "bart") (bart/fetch trip)
    (= (:agency trip) "muni") (muni/fetch trip)
    :else trip))

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
  (let [trips (map goog/parse-route routes)]
    ; (println "before map")
    ; (println (count (apply concat (map fetch-agency-data trips))))
    ; (println "after map")
    ; (println (count (into #{} (apply concat (map fetch-agency-data trips)))))
    (apply concat (map fetch-agency-data (make-uniq trips)))))

(defn process
  [body]
  (if-let [{:keys [origin dest]} body]
    (let [{:keys [status headers body error]} @(http/get (goog/build-url body))]
      (if error
        error
        {:status 200
          :headers {"Content-Type" "application/json"}
          :body (generate-string (parse-results (parse-string body true)))}))
    {:status 400
     :body "fail"}))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (POST "/" {body :body} (process body))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body {:keywords? true})
      (middleware/wrap-json-response)))

