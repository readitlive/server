(ns sir.bart
  (:require [org.httpkit.client :as http]
            [clojure.string :as str]))
;; let url = NSURL(string: "http://api.bart.gov/api/etd.aspx?cmd=etd&orig=" + data[0].originStationName + "&key=ZELI-U2UY-IBKQ-DT35")
(defn station-lookup [station]
  "TODO"
  station)

(defn build-url
  [{:keys [:originStationShortname] :as trip}]
  (println originStationShortname "----------------------")
  (str "http://api.bart.gov/api/etd.aspx?cmd=etd&orig="
       originStationShortname
       "&key=ZELI-U2UY-IBKQ-DT35"))


(defn fetch [trip]
  (let [{body :body} @(http/get (build-url trip))]
    (println body)))
