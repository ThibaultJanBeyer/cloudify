(ns cloudify.core
    (:require [cloudify.weather :as weather]))
;(use 'overtone.live)

;(definst foo [] (saw 220))

(defn -main [& args]
    (println (weather/download-and-parse)))
