(ns lostik.device
  "Communication primitives and device specific api commands."
  (:require
   [clojure.core.async :as a :refer [chan go-loop <! >!!]]
   [clojure.core.match :refer [match]]
   [clojure.string :as string]
   [serial.core :as sc]
   [lostik.util :as u]))

;; Getter and Setter protocol for the Device
(defprotocol IDeviceMut
  (mode! [this v])
  (get-mode [this])
  (msg-buffer! [this v])
  (get-msg-buffer [this]))

;; A lostick Device
;; The Device class is a container grouping all necessary part for communication
;; with the lostik device.
(deftype Device
  [port                           ;; serial port where the device is connected to the host
   ^:volatile-mutable mode        ;; the currently active mode of the device
   char-stream                    ;; a channel required to read single characters from the device
   ^:volatile-mutable msg-buffer  ;; buffer required to build messages from single characters
   msg-queue]                     ;; a channel on which messages recived from the device are put
  IDeviceMut
  (mode! [this v] (set! mode v))                ;; set the current mode of the device
  (get-mode [this] (. this mode))               ;; get the current mode of the device
  (msg-buffer! [this v] (set! msg-buffer v))    ;; (re-)set the msg-buffer of the device
  (get-msg-buffer [this] (. this msg-buffer)))  ;; get the msg-buffer of the device

(defn collect-characters!
  "Start collecting characters and assemble them to full device messages.

  Start a thread that reads from the devices character stream and puts the characters
  to the devices message buffer. If the message buffer holds a complete message, the
  message is pushed to the devices message queue and the buffer is flushed."
  [device]
  (a/thread []
    (loop []
      (when-let [stream  (a/<!! (.char-stream device))]
        (msg-buffer! device (conj (get-msg-buffer device) (.read stream)))
        (if (and (= 10 (get (get-msg-buffer device) (- (count (get-msg-buffer device)) 1)))   ; ASCII LF
                 (= 13 (get (get-msg-buffer device) (- (count (get-msg-buffer device)) 2))))  ; ASCII CR
          (do
            (a/>!! (.msg-queue device) (u/bytes->str (get-msg-buffer device)))
            (msg-buffer! device [])))
        (recur)))))

(defn print-msg
  "Start processing messages from the devices message queue.

  Start a thread that pull messages from the devices message queue and processes them
  in dependece from the devices current mode."
  [device]
  (a/thread
   (loop []
     (when-let [msg (a/<!! (.msg-queue device))]
       (println "Got a msg:")
       (println msg)
       (recur)))))


(defn new-device
  "Create a new Device.

  Takes the `path` where the device is accessible (e.g. /dev/ttyUSB0) and creates a
  Device object with all required fields. Initializes the serial port and puts the
  device in :IDLE mode. Returns the new device."
  [path]
  (let [port        (sc/open path
                             :baud-rate 57600
                             :stopbits sc/STOPBITS_1
                             :databits sc/DATABITS_8
                             :parity sc/PARITY_NONE)
        char-stream (a/chan  1)]
    (Device. port :IDLE char-stream [] (a/chan 1))))

(defn enable-device!
  "Enable the device.

  Start listening on the character stream read from the serial port, start the
  message processing and put device into :SYS mode."
  [device handler]
  (mode! device :SYS)
  (sc/listen! (.port device) (fn [c] (a/>!! (.char-stream device) c)))
  (collect-characters! device)
  (handler device)
  ;(process-msg! device)
  :ok)

(defn cmd!
  "Execute a command on the device.

  Takes the command as string. Takes care of appending the CRLF at the end of the
  command and converts the string to bytes."
  [device cmd & args]
  (let [cmd'  (if (empty? args)
                cmd
                (str cmd " " (string/join " " args)))]
    (sc/write (.port device) (u/str->bytes (u/w-crlf cmd')))
    :ok))

(defn device-info!
  "Execute device info command."
  [device]
  (cmd! device "sys get ver")
  :ok)

(defn led!
  "Set the state of the devices LEDs"
  [device state color]
  (match [state color]
    [:on  :blue]  (cmd! device "sys set pindig GPIO10 1")
    [:off :blue]  (cmd! device "sys set pindig GPIO10 0")
    [:on  :red]   (cmd! device "sys set pindig GPIO11 1")
    [:off :red]   (cmd! device "sys set pindig GPIO11 0")
    :else (println "Options are: state[:on :off], color[:blue :red]"))
  :ok)

(defn reset-device!
  "Reset the device."
  [device]
  (cmd! device "sys reset"))

(defn version
  "Get the status of the device."
  [device]
  (cmd! device "sys get ver"))


;; Join a LoRa network either with OTAA or ABP
(defmulti join-lora! (fn [type _ _ _ _] type))

;; Perform an OTAA join with the given credentials.
(defmethod join-lora! :otaa [_ device app-eui app-key device-eui]
  (cmd! device (str "mac set appeui " app-eui))
  (Thread/sleep 100)
  (cmd! device (str "mac set appkey " app-key))
  (Thread/sleep 100)
  (cmd! device (str "mac set deveui " device-eui))
  (Thread/sleep 100)
  (cmd! device (str "mac join otaa"))
  (Thread/sleep 100)
  :ok)

;; Perform an ABP join with the given credentials.
(defmethod join-lora! :abp [_ device dev-addr app-skey nwk-skey]
  (cmd! device (str "mac set devaddr " dev-addr))
  (Thread/sleep 100)
  (cmd! device (str "mac set appskey " app-skey))
  (Thread/sleep 100)
  (cmd! device (str "mac set nwkskey " nwk-skey))
  (Thread/sleep 100)
  (cmd! device (str "mac join abp"))
  (Thread/sleep 100)
  :ok)

;; default handler for join-lora! method. Raise exception.
(defmethod join-lora! :else []
  (ex-info "Invalid! Either :otaa or :abp join is possible." {}))
