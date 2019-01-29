(ns orchis.command.runner
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [chan <! >!] :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def child-process
  (nodejs/require "child_process"))

(defn exec 
  ([command]
   (-> child-process
       (.exec command)))
  ([command callback]
   (-> child-process
       (.exec command callback))))

(defn exec-sync [command]
  (-> child-process
      (.execSync command)))

(defn spawn
  ([command]
   (-> child-process
       (.spawn command)))
  ([command args]
   (let [args (clj->js args)]
     (-> child-process
         (.spawn command args))))
  ([command args options]
   (let [args (clj->js args)]
     (-> child-process
         (.spawn command args options)))))

(defn data->ch-fn [ch]
  (fn [data]
    (go
      (>! ch data)
      (async/close! ch))))

(defn spawn->ch [command args]
  (let [ps (spawn command args)
        outch (chan)
        errch (chan)
        codech (chan)]
    (-> ps
        (.-stdout)
        (.setEncoding "utf8"))
    (-> ps
        (.-stdout)
        (.on "data" (data->ch-fn outch)))
    (-> ps
        (.-stderr)
        (.on "data" (data->ch-fn errch)))
    (-> ps
        (.on "close" (data->ch-fn codech)))
    {:process ps
     :outch outch
     :errch errch
     :codech codech}))

(comment
  (exec "lx" (fn [err stdout stderr]
               (println "stderr: " stderr)))
  (exec-sync "ls -la")
  (go
    (let [spawn-ps (spawn->ch "git" ["log" "-1" "--pretty=%B"])
          code (-> (<! (get spawn-ps :codech))
                   (js/parseInt))]
      (if (= code 0)
        (println (<! (get spawn-ps :outch)))
        (println (<! (get spawn-ps :errch))))))
)
