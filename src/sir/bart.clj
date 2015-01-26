(ns sir.bart
  (:use cheshire.core)
  (:require [org.httpkit.client :as http]
            [clojure.xml :as xml]
            [clojure.string :as str]))

(defn parse [s]
  (xml/parse
    (java.io.ByteArrayInputStream. (.getBytes s))))

(defn gen-trips [times trip]
  (map
    #(into trip {:departureTime (+ (System/currentTimeMillis) (* (read-string %) 1000 60))})
    times))

(defn get-minutes-from-etd [etd]
  (map
    #(get-in % [:content 0 :content 0])
    (filter
      #(= (:tag %) :estimate)
      (:content etd))))

(defn get-etd-for-eol [body station-code]
  (nth (filter
    (fn [etd]
      (= (str/lower-case (get-in etd [:content 1 :content 0])) station-code))
    (->>
      body
      xml-seq
      (filter #(= (:tag %) :etd)))) 0))

(defn get-departure-times [body station-code]
  (get-minutes-from-etd (get-etd-for-eol body station-code)))

(defn process-data [trip body]
  (gen-trips (get-departure-times body (:eolStationCode trip)) trip))

(defn build-url
  [trip]
  (str "http://api.bart.gov/api/etd.aspx?cmd=etd&orig="
       (:originStationCode trip)
       "&key=ZELI-U2UY-IBKQ-DT35"))

(defn fetch [trip]
  (let [{body :body error :error} @(http/get (build-url trip))]
    (if error
      (println error "<--------------- error fetching bart")
      (process-data trip (parse body)))))

(def station-data {
  :12th-St-Oakland-City-Center "12th"
  :16th-St-Mission "16th"
  :19th-St-Oakland "19th"
  :24th-St-Mission "24th"
  :Ashby "ashb"
  :Balboa-Park "balb"
  :Bay-Fair "bayf"
  :Castro-Valley "cast"
  :Civic-Center-UN-Plaza "civc"
  :Coliseum-Oakland-Airport "cols"
  :Colma "colm"
  :Concord "conc"
  :Daly-City "daly"
  :Downtown-Berkeley "dbrk"
  :Dublin-Pleasanton "dubl"
  :El-Cerrito-del-Norte "deln"
  :El-Cerrito-Plaza "plza"
  :Embarcadero "embr"
  :Fremont "frmt"
  :Fruitvale "ftvl"
  :Glen-Park "glen"
  :Hayward "hayw"
  :Lafayette "lafy"
  :Lake-Merritt "lake"
  :MacArthur "mcar"
  :Millbrae "mlbr"
  :Montgomery-St "mont"
  :North-Berkeley "nbrk"
  :North-Concord-Martinez "ncon"
  :Oakland-Intl-Airport "oakl"
  :Orinda "orin"
  :Pittsburg-Bay-Point "pitt"
  :Pleasant-Hill "phil"
  :Powell-St "powl"
  :Richmond "rich"
  :Rockridge "rock"
  :San-Bruno "sbrn"
  :San-Francisco-Intl-Airport "sfia"
  :San-Leandro "sanl"
  :South-Hayward "shay"
  :South-San-Francisco "ssan"
  :Union-City "ucty"
  :Walnut-Creek "wcrk"
  :West-Dublin "wdub"
  :West-Oakland "woak" })

(defn normalize-name [string]
  (str/replace (str/replace (str/replace (str/replace string " " "-") "St." "St") "/" "-") "'" ""))

(defn station-lookup [station-name]
  (get station-data (keyword (normalize-name station-name))))
