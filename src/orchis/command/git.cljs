(ns orchis.command.git
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [chan <! >!] :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def simple-git
  (nodejs/require "simple-git"))

(def cwd
  (-> nodejs/process
      .cwd))

(defn log [option]
  (let [ch (chan)]
    (go
      (-> (simple-git cwd)
          (.log (clj->js option)
                (fn [err out]
                  (go
                    (->> {:out out
                          :err err}
                         (>! ch))
                    (async/close! ch))))))
    ch))

(defn parse-log [log]
  (when-not (nil? log)
    {:hash (.-hash log)
     :date (.-date log)
     :message (.-message log)
     :author-name (.-author_name log)
     :author-email (.-author_email log)}))

(defn latest-log []
  (let [ch (chan)]
    (go
      (let [logs (<! (log {}))
            latest (-> (get logs :out)
                       (.-all)
                       (js->clj)
                       (first)
                       (parse-log))]
        (when-not (nil? latest)
          (>! ch latest))
        (async/close! ch)))
    ch))

(defn latest-tag []
  (let [ch (chan)]
    (go
      (-> (simple-git cwd)
          (.raw
            (clj->js ["describe"
                      "--abbrev=0"
                      "--tags"])
            (fn [err out]
              (go
                (->> {:out out
                      :err err}
                     (>! ch))
                (async/close! ch))))))
    ch))

(defn diff
  ([]
   (diff []))
  ([opts]
   (let [ch (chan)]
     (go
       (-> (simple-git cwd)
           (.diff (clj->js opts)
                  (fn [err out]
                    (go
                      (->> {:out out
                            :err err}
                           (>! ch))
                      (async/close! ch))))))
     ch)))

(comment
  (go
    (println (<! (latest-log))))
  (go
    (println (<! (latest-tag))))
  (go
    (println (<! (diff ["HEAD^^"]))))
  (go
    (println (<! (diff))))
)


