(ns orchis.command.git
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [chan <! >!] :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def simple-git
  (nodejs/require "simple-git"))

(defn log [option]
  (let [ch (chan)]
    (go
      (-> (simple-git)
          (.log (clj->js option)
                (fn [err out]
                  (go
                    (->> {:out out
                          :err err}
                         (>! ch)))))))
    ch))

(defn parse-log [log]
  {:hash (.-hash log)
   :date (.-date log)
   :message (.-message log)
   :author-name (.-author_name log)
   :author-email (.-author_email log)})

(defn latest-log []
  (let [ch (chan)]
    (go
      (let [logs (<! (log {}))
            latest (-> (get logs :out)
                       (.-latest)
                       (parse-log))]
        (>! ch latest)))
    ch))

(comment
  (go
    (println (<! (latest-log))))
)


