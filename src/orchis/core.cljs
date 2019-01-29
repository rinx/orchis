(ns orchis.core
  (:require [cljs.nodejs :as nodejs]
            [clojure.tools.cli :refer [parse-opts]]
            [cljs.core.async :refer [chan <! >!] :as async]
            [orchis.command.runner :as command.runner]
            [orchis.command.git :as command.git])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(nodejs/enable-util-print!)

(def options-spec
  [["-h" "--help"]])

(defn run [args]
  (parse-opts args options-spec))

(defn -main []
  (go
    (let [latest-log (<! (command.git/latest-log))]
      (println latest-log)
      (println (run (.-argv nodejs/process))))))

(set! *main-cli-fn* -main)


