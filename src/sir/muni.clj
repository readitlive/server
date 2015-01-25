(ns sir.muni
  (:import (java.net URLEncoder))
  (:require [org.httpkit.client :as http]
            [clojure.string :as str]))

(defn url-safe [url]
  (some-> url str (URLEncoder/encode "UTF-8") (.replace "+" "%20")))

(defn build-url
  [trip]
  (str "http://services.my511.org/Transit2.0/GetNextDeparturesByStopName.aspx?token=83d1f7f4-1d1e-4fc0-a070-162a95bd106f&agencyName=SF-MUNI&stopName="
       (str/replace (url-safe (:originStationName trip)) "%26" "and")))

(defn fetch [trip]
  (println (build-url trip))
  (let [{body :body error :error} @(http/get (build-url trip))]
    (if error
      (println error "<--------------- error fetching muni")
      (println body "<=----------body from muni"))
    trip))
