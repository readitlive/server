(ns sir.goog)

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

(defn parse-results
  [{:keys [routes status]}]
    {:num (count routes)})

