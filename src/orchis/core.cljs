(ns orchis.core
  (:require [cljs.nodejs :as nodejs]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [cljs.core.async :refer [chan <! >!] :as async]
            [orchis.subcommand :as subcommand])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(nodejs/enable-util-print!)

(defn usage [options-summary]
  (->> ["Usage: orchis subcommand [options]"
        ""
        "Options:"
        options-summary
        ""
        "Subcommands:"
        "  semver             commit-comment based semantic versioning"
        "  semver-tag         semver and tagging"
        "  semver-tag-push    semver, tagging and pushtags"]
       (string/join "\n")))

(def options-spec
  [["-h" "--help" "Show usage"]
   [nil "--remote" "remote [default: origin]"
    :id :remote
    :default "origin"]])

(defn exit [exitcode message]
  (when (some? message) (println message))
  (.exit nodejs/process exitcode))

(defn runsc [{:keys [code message]}]
  (exit code message))

(defn run [args]
  (go
    (let [{:keys [options arguments errors summary]}
          (parse-opts args options-spec)
          subcommand (first (drop 2 arguments))
          sargs (drop 3 arguments)]
      (cond
        (get options :help) (exit 0 (usage summary))
        (= subcommand "semver") (runsc
                                  (<! (subcommand/semver)))
        (= subcommand "semver-tag") (runsc
                                      (<! (subcommand/semver-tag)))
        (= subcommand "semver-tag-push") (runsc
                                           (<! (subcommand/semver-tag-push options)))
        :else (exit 1 (usage summary))))))

(defn -main []
  (run (.-argv nodejs/process)))

(set! *main-cli-fn* -main)


