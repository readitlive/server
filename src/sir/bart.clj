(ns sir.bart
  (:require [org.httpkit.client :as http]
            [clojure.string :as str]))

(defn station-lookup [station]
  "TODO"
  station)

(defn build-url
  [trip]
  (str "http://api.bart.gov/api/etd.aspx?cmd=etd&orig="
       (:originStationShortname trip)
       "&key=ZELI-U2UY-IBKQ-DT35"))


(defn fetch [trip]
  trip)
  ; (let [{body :body} @(http/get (build-url trip))]
  ;   (println body)))


; (def lookup "["12th St. Oakland City Center" : "12th",
;     "16th St. Mission":"16th",
;     "19th St. Oakland":"19th",
;     "24th St. Mission":"24th",
;     "Ashby":"ashb",
;     "Balboa Park":"balb",
;     "Bay Fair":"bayf",
;     "Castro Valley":"cast",
;     "Civic Center/UN Plaza":"civc",
;     "Coliseum/Oakland Airport":"cols",
;     "Colma":"colm",
;     "Concord":"conc",
;     "Daly City":"daly",
;     "Downtown Berkeley":"dbrk",
;     "Dublin/Pleasanton":"dubl",
;     "El Cerrito del Norte":"deln",
;     "El Cerrito Plaza":"plza",
;     "Embarcadero":"embr",
;     "Fremont":"frmt",
;     "Fruitvale":"ftvl",
;     "Glen Park":"glen",
;     "Hayward":"hayw",
;     "Lafayette":"lafy",
;     "Lake Merritt":"lake",
;     "MacArthur":"mcar",
;     "Millbrae":"mlbr",
;     "Montgomery St.":"mont",
;     "North Berkeley":"nbrk",
;     "North Concord/Martinez":"ncon",
;     "Orinda":"orin",
;     "Pittsburg/Bay Point":"pitt",
;     "Pleasant Hill":"phil",
;     "Powell St.":"powl",
;     "Richmond":"rich",
;     "Rockridge":"rock",
;     "San Bruno":"sbrn",
;     "San Francisco Int'l Airport":"sfia",
;     "San Leandro":"sanl",
;     "South Hayward":"shay",
;     "South San Francisco":"ssan",
;     "Union City":"ucty",
;     "Walnut Creek":"wcrk",
;     "West Dublin":"wdub",
;     "West Oakland":"woak"
; ]")
