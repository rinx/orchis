(ns user
  (:require
    [clojure.tools.namespace.repl :as repl
     :refer [refresh refresh-all]]
    [cljs.repl :as cljsrepl]
    [cljs.repl.node :as node]))

(defn dev []
  (cljsrepl/repl (node/repl-env)))

