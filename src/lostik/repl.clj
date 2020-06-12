(ns lostik.repl
  "REPL boilerplate to interactively connect to lostik and develop this library."
  (:require
   [clojure.core.async :as a]
   [geraet.util :refer [normalize->eui]]
   [lostik.device :as dv :refer [cmd! device-info! led! enable-device! new-device print-msg]]
   [lostik.lora :as l]
   [serial.core :as sc]
   [lostik.util :as u]))

(comment
  (defn join-ttn
    "Perform an OTAA join on The Things Network using example credentials."
    [lora]
    (let [app-eui       "70B3D57ED0030333"
          app-key       "70DD7BFD8ED660DC52AE7F7D0BDB87B9"
          device-eui    "005A344421CF7401"]
      (l/join! lora app-eui app-key device-eui)))

 "Example on how to setup lostik device, join the The Things Network and send
  example payload ASCII bytes 'abc'."
 (def lora (l/new-lora "/dev/ttyUSB0"))
 (l/enable! lora)
 (join-ttn lora)
 (send-data! lora (u/str->bytes "abc")))
