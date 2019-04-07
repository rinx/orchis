(ns cljs.user
  (:require [cljs.nodejs :as nodejs]
            [cljs.core.async :refer [chan <! >!] :as async]
            [orchis.command.git :as command.git])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(comment
  (in-ns 'orchis.core)
  
  (in-ns 'orchis.command.git)
  (in-ns 'orchis.command.runner)
  (in-ns 'orchis.command.octokit)
  
  (in-ns 'orchis.step.semver)
  (in-ns 'orchis.step.github))
