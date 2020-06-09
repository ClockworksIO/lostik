(ns lostik.core
  "Lostik tools used on the repl"
  (:require
   [clojure.pprint :refer [pprint]]
   [clojure.core.async :as a :refer [chan go-loop <! >!!]]
   [clojure.core.match :refer [match]]
   [serial.core :as sc]
   [serial.util :as su]
   [lanterna.terminal :as t]
   [lostik.device :as dv]
   [lostik.util :as u])
  (:gen-class))
;
; ;; The serial port.
; (def port (atom nil))
; ;; Input stream that reads single characters from the serial port.
; (def istream (a/chan 1))
; ;; Buffer used to assemble full messages sent from lostik device.
; (def msg-buffer (atom []))
; ;; Message stream that forwards full messages sent from the lostik device.
; (def msg-stream (a/chan 1))
;
; ;; Read characters from serial port and assemble full messages.
; ;; Reads a single character, puts it into the message buffer and checks
; ;; if a complete message is available. When the full message is available,
; ;; then the message is forwarded to the message channel and the buffer is
; ;; cleared.
; (a/go-loop []
;   (let [data  (a/<! istream)]
;     (swap! msg-buffer conj (.read data))
;     (if (and (= 10 (get @msg-buffer (- (count @msg-buffer) 1)))   ; ASCII LF
;              (= 13 (get @msg-buffer (- (count @msg-buffer) 2))))  ; ASCII CR
;       (do
;         (a/put! msg-stream (u/bytes->str @msg-buffer))
;         (reset! msg-buffer [])))
;     (recur)))
;
; ;; Take complete messages sent by the lostik device and display them on the
; ;; console.
; (a/go-loop []
;   (let [msg  (a/<! msg-stream)]
;     (println "received message:")
;     (println msg)
;     (recur)))


(def term (t/get-terminal :text))

(def put-character-to-term (partial t/put-character term))
(def write #(dorun (map put-character-to-term %)))

(defn -main
  "Application Execution"
  [& args]
  (t/start term)
  (write "Welcome to lostik."))
