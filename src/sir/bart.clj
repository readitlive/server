(ns sir.bart
  (:require [org.httpkit.client :as http]
            [clojure.string :as str]))

(defn station-lookup [station]
  "TODO"
  station)

(defn build-url
  [trip]
  (println trip "----------------------")
  (str "http://api.bart.gov/api/etd.aspx?cmd=etd&orig="
       (:originStationShortname trip)
       "&key=ZELI-U2UY-IBKQ-DT35"))


(defn fetch [trip]
  (println (build-url trip))
  trip)
  ; (let [{body :body} @(http/get (build-url trip))]
  ;   (println body)))
