(defproject lostik "0.1.0"
  :description "Repl based tool for the lostik LoRaWAN experimentation stick."
  :url "https://github.com/ClockworksIO/lostick"
  :license {:name "Apache 2.0 License"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :plugins [[lein-tools-deps "0.4.1"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files ["deps.edn" :install :user :project]
                           :clojure-executables ["/usr/bin/clojure"]}

  :main ^:skip-aot lostik.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}

  :repl-options {:init-ns lostik.repl})
