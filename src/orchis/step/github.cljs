(ns orchis.step.github
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [chan <! >!] :as async]
            [clojure.string :as string]
            [orchis.command.git :as command.git]
            [orchis.command.github :as command.github])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn github-release [url token owner repo]
  (let [ch (chan)]
    (go
      (let [{:keys [out err]} (<! (command.git/latest-tag))]
        (when (and (nil? err) (some? out))
          (let [latest-tag (string/trim-newline out)
                params {:owner owner
                        :repo repo
                        :tag-name latest-tag}
                {:keys [result error]} (<! (command.github/github-release
                                             url token params))]
            (when (and (nil? error) (some? result))
              (>! ch result)))))
      (async/close! ch))
    ch))
