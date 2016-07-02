(ns cloudify.core
    (:require [clojure.data.json :as json])
    (:require [clj-http.client :as client]))
;(use 'overtone.live)

;(definst foo [] (saw 220))

(def weatherURL "http://api.openweathermap.org/data/2.5/forecast?id=2946447&appid=a5982036f8e98737775ab26d08f4936d")

(defn map-3h [main]
    [(Math/round (- (get-in main [:main :temp]) 273.15))
     (get-in main [:main :pressure])
     (get-in main [:main :humidity])
     (get-in main [:clouds :all])
     (get-in main [:wind :speed])])

(defn parse-data [body]
    (filter #(not-any? nil? %1)
        (map map-3h
            (:list (json/read-str body :key-fn keyword)))))
    
(defn download-and-parse [url]
    (parse-data ((client/get url) :body)))

(defn -main [& args]
    (println (download-and-parse weatherURL)))
