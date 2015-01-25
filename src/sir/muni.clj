(ns sir.muni
  (:require [clojure.string :as str]))



(defn build-url
  [trip]
  (str "http://services.my511.org/Transit2.0/GetNextDeparturesByStopName.aspx?token=83d1f7f4-1d1e-4fc0-a070-162a95bd106f&agencyName=SF-MUNI&stopName="
       (:originStationName trip)))




(defn fetch [trip]
  (println (build-url trip))
  (build-url trip))
  ; trip)