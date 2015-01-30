(ns sir.muni
  (:import (java.net URLEncoder))
  (:require [org.httpkit.client :as http]
            [clojure.xml :as xml]
            [clojure.string :as str]))

(defn url-safe [url]
  (some-> url str (URLEncoder/encode "UTF-8") (.replace "+" "%20")))

(defn parse [s]
  (xml/parse
    (java.io.ByteArrayInputStream. (.getBytes s))))

;(defn normalize-station-name [name]
;  )

; TODO: do replacements:
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("Metro ", withString: "")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Outbd", withString: " Outbound")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Outbound", withString: " Outbound")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Inbd", withString: " Inbound")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Inbound", withString: " Inbound")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Downtn", withString: " Inbound")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Downtown", withString: " Inbound")


(defn gen-trips [times trip]
  (map
    (fn [minutes]
      ; minutes is either a number or "Leaving"
      (if (number? (read-string minutes))
        (into trip {:departureTime (+ (System/currentTimeMillis) (* (read-string minutes) 1000 60))})
        (into trip {:departureTime (+ (System/currentTimeMillis) (* 1000 60))})))
    times))
;
;(defn get-minutes-from-etd [etd]
;  (map
;    #(get-in % [:content 0 :content 0])
;    (filter
;      #(= (:tag %) :estimate)
;      (:content etd))))
;
;(defn get-etd-for-eol [body station-code]
;  (nth (filter
;         (fn [etd]
;           (= (str/lower-case (get-in etd [:content 1 :content 0])) station-code))
;         (->>
;           body
;           xml-seq
;           (filter #(= (:tag %) :etd)))) 0))
;
;(defn get-departure-times [body station-code]
;  (get-minutes-from-etd (get-etd-for-eol body station-code)))
;
;(defn process-data [trip body]
;  (gen-trips (get-departure-times body (:eolStationCode trip)) trip))

(defn direction-from-trip [{eolStationName :eolStationName}]
  "Outbound")

(defn get-times-from-departure [direction]
  (map
    #(get-in % [:content 0])
    (get direction 0))) ; no idea why this works or is needed

(defn get-departures-for-direction [departures trip]
  (let [departure
    (reduce
      (fn [coll item]
        (if (= (direction-from-trip trip) (get-in item [:content 0 :content 0 :attrs :Code]))
          (conj coll (get-in item [:content 0 :content 0 :content 0 :content 0 :content 0 :content]))
          (if (= (direction-from-trip trip) (get-in item [:content 1 :content 0 :attrs :Code]))
            (conj coll (get-in item [:content 1 :content 0 :content 0 :content 0 :content 0 :content]))
            coll)))
      []
      departures)]
  (get-times-from-departure departure)))

(defn get-route-for-line [routes {lineCode :lineCode}]
  (into [] (filter
    (fn [item]
      (= lineCode (get-in item [:attrs :Code])))
    routes)))

(defn get-routes [routes]
  (let [route-or-routes (get-in routes [:content 0 :content 0 :content 0 :content 0])]
    (if (vector? route-or-routes)
      route-or-routes
      (conj [] route-or-routes))))

(defn get-departure-times [body trip]
  (get-departures-for-direction (get-route-for-line (get-routes body) trip) trip))

(defn process-data [trip body]
  (gen-trips (get-departure-times body trip) trip))

(defn build-url
  [trip]
  (str "http://services.my511.org/Transit2.0/GetNextDeparturesByStopName.aspx?token=83d1f7f4-1d1e-4fc0-a070-162a95bd106f&agencyName=SF-MUNI&stopName="
       (str/replace (url-safe (:originStationName trip)) "%26" "and")))

(defn fetch [trip]
  (println "--------------------")
  (println (build-url trip))
  (let [{body :body error :error} @(http/get (build-url trip))]
    (if error
      (println error "<--------------- error fetching muni")
      (process-data trip (parse body)))))