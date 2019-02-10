(ns orchis.subcommand
  (:require [cljs.nodejs :as nodejs]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [cljs.core.async :refer [chan <! >!] :as async]
            [orchis.step.semver :as step.semver])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn semver []
  (let [ch (chan)]
    (go
      (let [res (<! (step.semver/semver))]
        (when (some? res)
          (println res)))
      (>! ch {:code 0})
      (async/close! ch))
    ch))

(defn semver-tag []
  (let [ch (chan)]
    (go
      (let [res (<! (step.semver/semver-tag))]
        (when (some? res)
          (println res)))
      (>! ch {:code 0})
      (async/close! ch))
    ch))

(defn semver-tag-push [options]
  (let [ch (chan)
        remote (get options :remote)]
    (go
      (if (some? remote)
        (let [res (<! (step.semver/semver-tag-push remote))]
          (when (some? res)
            (println res))
          (>! ch {:code 0}))
        (>! ch {:code 1
                :message "'remote' is required."}))
      (async/close! ch))
    ch))

(defn simple-semver [args]
  (let [ch (chan)
        semver-type (first args)]
    (go
      (if (some? semver-type)
        (do
          (-> semver-type
              (string/lower-case)
              (keyword)
              (step.semver/simple-semver)
              (<!)
              (println))
          (>! ch {:code 0}))
        (>! ch {:code 1
                :message "requires 'patch', 'minor' or 'major' argument."}))
      (async/close! ch))
    ch))

