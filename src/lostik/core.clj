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

(defn -main
  "Application Execution"
  [& args]
  (println "i do nothing yet."))
