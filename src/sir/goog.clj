(ns sir.goog
  (:require
    [sir.bart :as bart]
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
;; eolStationName
;; agency
;; lineName
;; lineCode
;; agency

(defn get-departure-time [step]
  (get-in step [:transit_details :departure_time :value]))

(defn get-line-name [step]
  (get-in step [:transit_details :line :name]))

(defn get-line-code [step]
  (get-in step [:transit_details :line :short_name]))

(defn get-transit-type [step]
  (get-in step [:transit_details :line :vehicle :type]))

(defn get-origin-station-name [step]
  (get-in step [:transit_details :departure_stop :name]))

(defn get-origin-station-loc [{start :start_location}]
  {:lat (:lat start)
   :lon (:lng start)})

(defn get-eol-station-name [step]
  (let [strings ["Train towards", "Metro rail towards", "Bus towards"]]
    (str/trim
      (reduce
        (fn [result replacer]
          (str/replace result replacer ""))
        (:html_instructions step)
        strings))))

(defn process-caltrain [step]
  { :originStationName (get-origin-station-name step)
    :originStationLatLon (get-origin-station-loc step)
    :departureTime (get-departure-time step)
    :eolStationName (get-eol-station-name step)
    :lineName (get-line-name step)
    :agency "caltrain"})

(defn process-bart [step]
  { :originStationName (get-origin-station-name step)
    :originStationShortname (bart/station-lookup (get-origin-station-name step))
    :originStationLatLon (get-origin-station-loc step)
    :eolStationName (get-eol-station-name step)
    :lineName (get-line-name step)
    :agency "bart"})

(defn process-muni [step]
  { :originStationName (get-origin-station-name step)
    :originStationLatLon (get-origin-station-loc step)
    :eolStationName (get-eol-station-name step)
    :lineName (get-line-name step)
    :lineCode (get-line-code step)
    :transitType (get-transit-type step)
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

(defn remove-nils [thing]
  (into {} (filter #(not= nil %) thing)))

(defn not-bus? [step]
  (not= (:transitType step) "BUS"))

; TODO:
; filter out routes where the bus is too long, routes where supported transit type is after second,

(defn filter-steps [route]
  (remove-nils route))
    ; (filter not-bus? route)))

(defn parse-route [route]
  (filter-steps
    (map parse-step (:steps (get (:legs route) 0)))))

