(ns lostik.lora
  "LoRaWAN abstractions for lostik."
  (:require
   [clojure.string :as string]
   [clojure.core.async :as a]
   [lostik.device :as dv]
   [lostik.util :as u]))


;; Interface for LoRa class
(defprotocol ILoRa
  (get-device [this]))        ; return a reference to the device of the lora object

;; The LoRa class provides an abstraction on top of the lostik.device/Device
;; class to handle LoRaWAN connections and data exchange.
(deftype LoRa
  [device]
  ILoRa
  (get-device [this] (. this device)))


(defn new-lora
  "Create a new LoRa instance.

  Takes the path of the lostik device (e.g. '/dev/ttyUSB0'). Takes care of creating
  the encapsulated Device."
  [path]
  (LoRa. (dv/new-device path)))

(defn lora-msg-handler
  "Handler for messages received from the device.

  This handler is build to work with a lostik device setup for lora communication.
  It spawns a thread that reads messages received from the hardware device."
  [device]
  (a/thread
   (loop []
     (when-let [msg (a/<!! (.msg-queue device))]
       (let [msg' (string/trim msg)]
         (case (.get-mode device)
           :IDLE  (println "IDLE: " msg')
           :SYS   (println "SYS: " msg')
           :JOIN-PENDING  (if (= "accepted" msg')
                            (do
                              (.mode! device :JOINED)
                              (println "JOINED: " msg'))
                            (do
                              (.mode! device :SYS)
                              (println "FAILED: lora join failed! dev: " msg')))
           :JOINED (println "JOINED: " msg')
           ; else
           (do
             (println "UNDEFINED: " msg'))))
       (recur)))))

(defn enable!
  "Enable the LoRa instance to begin transceiving.

  Enables the encapsulated Device instance and sets up a handler thread for processing
  messages received from the hardware."
  ([lora] (dv/enable-device! (.get-device lora) lora-msg-handler) :ok)
  ([lora msg-handler] (dv/enable-device! (.get-device lora) msg-handler) :ok))

(defn join!
  "Perform an OTAA join with the given credentials."
  [lora app-eui app-key device-eui]
  (dv/join-lora! :otaa (.get-device lora) app-eui app-key device-eui)
  (.mode! (.get-device lora) :JOIN-PENDING))

(defn join-abp!
  "Perform an OTAA join with the given credentials."
  [lora dev-addr app-skey nwk-skey]
  (dv/join-lora! :abp (.get-device lora) dev-addr app-skey nwk-skey)
  (.mode! (.get-device lora) :JOIN-PENDING))

(defn send-data!
  "Send data frame over the network. Takes the data as byte sequence.
  Does not care for byte order! Send the bytes in the order they are
  in the given data sequence!"
  [lora data & {:keys [confirm? port]
                  :or {confirm? false, port 1}}]
  (let [cnf?  (if confirm? "cnf" "uncnf")]
    (dv/cmd! (.get-device lora) (str "mac tx "cnf?" "port) (u/bytes->hexs data))
    :ok))
