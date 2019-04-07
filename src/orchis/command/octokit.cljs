(ns orchis.command.octokit
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [chan <! >!] :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def octokit-rest
  (nodejs/require "@octokit/rest"))

(defn octokit
  ([url]
   (octokit url nil))
  ([url token]
   (octokit-rest.
     {:auth (when token (str "token " token))
      :baseUrl url
      :request {:timeout 300000}})))

(defn await [promise]
  (let [ch (chan)]
    (-> promise
        (.then (fn [res] (async/put! ch {:result (js->clj res)}))
               (fn [err] (async/put! ch {:error (js->clj err)})))
        (.catch (fn [err] (async/put! ch {:error (js->clj err)}))))
    ch))

(defn github-release [url token {:keys [owner repo tag-name]}]
  (-> (octokit url token)
      (.-repos)
      (.createRelease (clj->js {:owner owner
                                :repo repo
                                :tag_name tag-name}))
      (await)))

(comment
  (go
    (-> (octokit "https://api.github.com")
        (.-repos)
        (.listReleases (clj->js {:owner "rinx"
                                 :repo "orchis"}))
        (await)
        (<!)
        (println)))
  )


