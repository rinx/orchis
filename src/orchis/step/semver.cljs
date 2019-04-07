(ns orchis.step.semver
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [chan <! >!] :as async]
            [clojure.string :as string]
            [orchis.command.git :as command.git])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def default-version-str "0.0.0")
(def delimiter ".")

(defn str->version [s]
  (let [matches (re-seq #"^(\d+)\.(\d+)\.(\d+)$" s)]
    (when matches
      (let [matches (nth matches 0)]
        {:major (js/parseInt (nth matches 1))
         :minor (js/parseInt (nth matches 2))
         :patch (js/parseInt (nth matches 3))}))))

(defn version->str [{:keys [major minor patch]}]
  (str major delimiter minor delimiter patch))

(defn bump-patch
  ([version]
   (-> version
       (update :patch inc)))
  ([version default]
   (-> version
       (assoc :patch default))))

(defn bump-minor
  ([version]
   (-> version
       (bump-patch 0)
       (update :minor inc)))
  ([version default]
   (-> version
       (bump-patch 0)
       (assoc :minor default))))

(defn bump-major
  ([version]
   (-> version
       (bump-minor 0)
       (update :major inc)))
  ([version default]
   (-> version
       (bump-minor 0)
       (assoc :major default))))

(defn fetch-latest-tag []
  (let [ch (chan)]
    (go
      (let [{out :out err :err} (<! (command.git/latest-tag))]
        (when (and (nil? err) (some? out))
          (>! ch out))
        (async/close! ch)))
    ch))

(defn new-version [msg version]
  (cond
    (re-seq #"\[(major|MAJOR)\]" msg) (bump-major version)
    (re-seq #"\[(minor|MINOR)\]" msg) (bump-minor version)
    (re-seq #"\[(patch|PATCH)\]" msg) (bump-patch version)
    :else nil))

(defn semver []
  (let [ch (chan)]
    (go
      (let [latest-log (<! (command.git/latest-log))
            latest-tag (-> (<! (fetch-latest-tag))
                           (or default-version-str)
                           (string/trim-newline))
            version (str->version latest-tag)
            latest-log-msg (get latest-log :message)
            new-version (new-version latest-log-msg version)]
        (when (some? new-version)
          (->> new-version
               (version->str)
               (>! ch)))
        (async/close! ch)))
    ch))

(defn semver-tag []
  (let [ch (chan)]
    (go
      (let [new-version (<! (semver))]
        (when new-version
          (let [{out :out err :err} (<! (command.git/tag new-version))]
            (when (and (nil? err) (some? out))
              (>! ch new-version))))
        (async/close! ch)))
    ch))

(defn semver-tag-push [remote]
  (let [ch (chan)]
    (go
      (let [new-version (<! (semver-tag))]
        (when new-version
          (let [{out :out err :err} (<! (command.git/push-tags remote))]
            (when (and (nil? err) (some? out))
              (>! ch new-version))))
        (async/close! ch)))
    ch))

(defn simple-semver [t]
  (let [ch (chan)]
    (go
      (let [latest-tag (-> (<! (fetch-latest-tag))
                           (or default-version-str)
                           (string/trim-newline))
            version (str->version latest-tag)
            new-version (case t
                          :major (bump-major version)
                          :minor (bump-minor version)
                          :patch (bump-patch version)
                          nil)]
        (when (some? new-version)
          (->> new-version
               (version->str)
               (>! ch)))
        (async/close! ch)))
    ch))

(comment
  (str->version "1.2.3")
  (version->str {:major 1 :minor 2 :patch 3})
  (go (println (get (<! (command.git/latest-log)) :message)))
  (go (println (<! (fetch-latest-tag))))
  (go
    (println
      (or (<! (fetch-latest-tag)) default-version-str)))
  (go
    (println
      (str->version
        (string/trim-newline
          (or (<! (fetch-latest-tag)) default-version-str)))))
  (go
    (println (<! (semver))))
  (go
    (println (<! (semver-tag)))))
