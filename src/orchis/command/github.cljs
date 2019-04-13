(ns orchis.command.github
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [chan <! >!] :as async]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn http-get
  ([endpoint]
   (http-get endpoint nil))
  ([endpoint token]
   (let [ch (chan)]
     (go
       (let [headers (cond-> {}
                       token (into {"Authorization" (str "token " token)}))
             response (<! (http/get
                            endpoint
                            {:with-credentials? false
                             :headers headers}))
             body (:body response)]
         (when body
           (>! ch body))
         (async/close! ch)))
     ch)))

(defn http-post
  ([endpoint]
   (http-post endpoint nil nil))
  ([endpoint params]
   (http-post endpoint nil params))
  ([endpoint token params]
   (let [ch (chan)]
     (go
       (let [headers (cond-> {}
                       token (into {"Authorization" (str "token " token)}))
             response (<! (http/post
                            endpoint
                            {:with-credentials? false
                             :headers headers
                             :json-params params}))
             body (:body response)]
         (when body
           (>! ch body))
         (async/close! ch)))
     ch)))

(defn github-release [url token {:keys [owner repo tag-name]}]
  (let [endpoint (str url "/repos/" owner "/" repo "/releases")
        params {:tag_name tag-name}]
    (http-post endpoint token params)))

(defn fetch-releases
  ([url params]
   (fetch-releases url nil params))
  ([url token {:keys [owner repo]}]
   (let [endpoint (str url "/repos/" owner "/" repo "/releases")]
     (http-get endpoint token))))

(comment
  (go
    (println
      (<! (fetch-releases "https://api.github.com"
                          {:owner "rinx"
                           :repo "orchis"}))))
  )


