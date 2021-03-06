(ns orchis.command.git
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [chan <! >!] :as async]
            [orchis.command.runner :as runner])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def simple-git
  (nodejs/require "simple-git"))

(def cwd
  (-> nodejs/process
      .cwd))

(defn standard-handler [ch]
  (fn [err out]
    (go
      (->> {:out out
            :err err}
           (>! ch))
      (async/close! ch))))

(defn log [option]
  (let [ch (chan)]
    (go
      (-> (simple-git cwd)
          (.log (clj->js option)
                (standard-handler ch))))
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
      (let [spawn-ps (runner/spawn->ch "git" ["describe"
                                       "--abbrev=0"
                                       "--tags"])
            code (-> (get spawn-ps :codech)
                     (<!)
                     (js/parseInt))]
        (if (= code 0)
          (>! ch {:out (<! (get spawn-ps :outch))})
          (>! ch {:err (<! (get spawn-ps :errch))}))
        (async/close! ch)))
    #_(go
      (-> (simple-git cwd)
          (.raw
            (clj->js ["describe"
                      "--abbrev=0"
                      "--tags"])
            (standard-handler ch))))
    ch))

(defn diff [& opts]
  (let [ch (chan)]
    (go
      (-> (simple-git cwd)
          (.diff (clj->js opts)
                 (standard-handler ch))))
    ch))

(defn tag [& args]
  (let [ch (chan)]
    (go
      (-> (simple-git cwd)
          (.tag (clj->js args)
                (standard-handler ch))))
    ch))

(defn push-tags [remote]
  (let [ch (chan)]
    (go
      (-> (simple-git cwd)
          (.pushTags remote
                (standard-handler ch))))
    ch))

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


