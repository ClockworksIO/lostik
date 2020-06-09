(ns lostik.repl
  "REPL boilerplate to interactively connect to lostik and develop this library."
  (:require
   [clojure.core.async :as a]
   [geraet.util :refer [normalize->eui]]
   [lostik.comm :as comm :refer [cmd! device-info! led! enable-device! new-device join! send-data! print-msg]]
   [serial.core :as sc]
   [lostik.util :as u]))



; (defrecord LoraCnx
;   [^mutable state
;    port
;    rx-source
;    app-eui
;    app-key
;    dev-eui])
;
; (defn update-state
;   [^LoraCnx self state]
;   (set! (.-state self) state))
;
; (defn new-loracnx
;   [port msg-src app-eui app-key dev-eui]
;   (LoraCnx. :READY port msg-src app-eui app-key dev-eui))
;
; (defn join!
;   [lora]
;   (comm/cmd! (.port lora) (str "mac set appeui " (.app-eui lora))))
;
; (defn lora-init
;   [lora])


(defn join-ttn
  "Perform an OTAA join on The Things Network using example credentials."
  [device]
  (let [app-eui       "70B3D57ED0030333"
        app-key       "70DD7BFD8ED660DC52AE7F7D0BDB87B9"
        device-eui    "005A344421CF7401"]
    (join! device app-eui app-key device-eui)))

(comment
 "Example on how to setup lostik device, join the The Things Network and send
  example payload ASCII bytes 'abc'."
 (def device (new-device "/dev/ttyUSB0"))
 (enable-device! device print-msg)
 (join-ttn device)
 (send-data! device  (u/str->bytes "abc")))
