(ns sir.goog
  (:require
    [sir.bart :as bart]
    [sir.muni :as muni]
    [clojure.string :as str]))



(defn build-url
  [{origin :origin dest :dest}]
  (str "https://maps.googleapis.com/maps/api/directions/json?origin="
       (origin :lat)
       ","
       (origin :lng)
       "&destination="
       (dest :lat)
       ","
       (dest :lng)
       "&key=AIzaSyB9JV82Cy-GFPTAbYy3HgfZOGT75KVp-dg&departure_time="
       (quot (System/currentTimeMillis) 1000)
       "&mode=transit&alternatives=true"))
;; Result object:
;; originStationName
;; originStationLatLon {:lat :lon}
;; departureTime
;; destStationName
;; agency
;; lineName
;; lineCode
;; agency

(defn fetch-data [trip]
  (cond
    (= (:agency trip) "bart") (bart/fetch trip)
    (= (:agency trip) "muni") (muni/fetch trip)
    :else trip))

(defn get-departure-time [step]
  (get-in step [:transit_details :departure_time :value]))

(defn get-line-name [step]
  (get-in step [:transit_details :line :name]))

(defn get-line-code [step]
  (get-in step [:transit_details :line :short_name]))

(defn get-origin-station-name [step]
  (get-in step [:transit_details :departure_stop :name]))

(defn get-origin-station-loc [{start :start_location}]
  {:lat (:lat start)
   :lon (:lng start)})

(defn get-dest-station-name [step]
  (let [strings ["Train towards", "Metro rail towards"]]
    (str/trim
      (reduce
        (fn [result replacer]
          (str/replace result replacer ""))
        (:html_instructions step)
        strings))))

(defn get-dest-station-name-bart [step]
  (str/trim (str/replace (:html_instructions step) #"Train towards" "")))

(defn process-caltrain [step]
  { :originStationName (get-origin-station-name step)
    :originStationLatLon (get-origin-station-loc step)
    :departureTime (get-departure-time step)
    :destStationName (get-dest-station-name step)
    :lineName (get-line-name step)
    :agency "caltrain"})

(defn process-bart [step]
  { :originStationName (bart/station-lookup (get-origin-station-name step))
    :originStationLatLon (get-origin-station-loc step)
    :departureTime (get-departure-time step)
    :destStationName (get-dest-station-name step)
    :lineName (get-line-name step)
    :agency "bart"})

(defn process-muni [step]
  { :originStationName (get-origin-station-name step)
    :originStationLatLon (get-origin-station-loc step)
    :departureTime (get-departure-time step)
    :destStationName (get-dest-station-name step)
    :lineName (get-line-name step)
    :lineCode (get-line-code step)
    :agency "muni"})

(defn agency-name-from-step [step]
  (if-let [agency-name (:name (get (get-in step [:transit_details :line :agencies]) 0))]
    agency-name
    nil))

(defn parse-step [step]
  (let [name (agency-name-from-step step)]
    (cond
      (= name "San Francisco Municipal Transportation Agency") (process-muni step)
      (= name "Bay Area Rapid Transit") (process-bart step)
      (= name "Caltrain") (process-caltrain step))))


(defn parse-route [route]
  (into {} (filter #(not= nil %) (map parse-step (:steps (get (:legs route) 0))))))

(defn parse-results
  [{:keys [routes status]}]
  (let [trips (map parse-route routes)]
    (println trips)
    (map fetch-data trips)
    trips))

