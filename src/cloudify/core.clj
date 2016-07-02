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

(demo 60
      (let [bpm     120
            ;; create pool of notes as seed for random base line sequence
            notes   [40 41 28 28 28 27 25 35 78]
            ;; create an impulse trigger firing once per bar
            trig    (impulse:kr (/ bpm 120))
            ;; create frequency generator for a randomly picked note
            freq    (midicps (lag (demand trig 0 (dxrand notes INF)) 0.25))
            ;; switch note durations
            swr     (demand trig 0 (dseq [1 6 6 2 1 2 4 8 3 3] INF))
            ;; create a sweep curve for filter below
            sweep   (lin-exp (lf-tri swr) -1 1 40 3000)
            ;; create a slightly detuned stereo sawtooth oscillator
            wob     (mix (saw (* freq [0.99 1.01])))
            ;; apply low pass filter using sweep curve to control cutoff freq
            wob     (lpf wob sweep)
            ;; normalize to 80% volume
            wob     (* 0.8 (normalizer wob))
            ;; apply band pass filter with resonance at 5kHz
            wob     (+ wob (bpf wob 1500 2))
            ;; mix in 20% reverb
            wob     (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

            ;; create impulse generator from given drum pattern
            kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
            ;; use modulated sine wave oscillator
            kick    (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
            ;; clip at max volume to create distortion
            kick    (clip2 kick 1)

            ;; snare is just using gated & over-amplified pink noise
            snare   (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
            ;; send through band pass filter with peak @ 2kHz
            snare   (+ snare (bpf (* 4 snare) 2000))
            ;; also clip at max vol to distort
            snare   (clip2 snare 1)]
   ;; mixdown & clip
  (clip2 (+ wob kick snare) 1)))