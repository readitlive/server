(ns sir.muni
  (:import (java.net URLEncoder))
  (:require [org.httpkit.client :as http]
            [clojure.string :as str]))

(defn url-safe [url]
  (some-> url str (URLEncoder/encode "UTF-8") (.replace "+" "%20")))


; TODO: do replacements:
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("Metro ", withString: "")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Outbd", withString: " Outbound")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Outbound", withString: " Outbound")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Inbd", withString: " Inbound")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Inbound", withString: " Inbound")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Downtn", withString: " Inbound")
; muniOriginStationName = muniOriginStationName.stringByReplacingOccurrencesOfString("/Downtown", withString: " Inbound")

(defn build-url
  [trip]
  (str "http://services.my511.org/Transit2.0/GetNextDeparturesByStopName.aspx?token=83d1f7f4-1d1e-4fc0-a070-162a95bd106f&agencyName=SF-MUNI&stopName="
       (str/replace (url-safe (:originStationName trip)) "%26" "and")))

(defn process-data [trip body]
  trip)

(defn fetch [trip]
  (let [{body :body error :error} @(http/get (build-url trip))]
    (if error
      (println error "<--------------- error fetching muni")
      (process-data trip body))))
