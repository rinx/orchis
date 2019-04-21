(ns orchis.core
  (:require [cljs.nodejs :as nodejs]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [cljs.core.async :refer [chan <! >!] :as async]
            [orchis.subcommand :as subcommand])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(nodejs/enable-util-print!)

(set! js/XMLHttpRequest (nodejs/require "xhr2"))

(defn usage [options-summary]
  (->> ["Usage: orchis subcommand [options]"
        ""
        "Options:"
        options-summary
        ""
        "Subcommands:"
        "  semver             commit-comment based semantic versioning"
        "  semver-tag         semver and tagging"
        "  semver-tag-push    semver, tagging and pushtags"
        "  simple-semver      simply returns bumped version"
        "  gh-release         create a release from latest tag"]
       (string/join "\n")))

(def options-spec
  [["-h" "--help" "Show usage"
    :id :help
    :default false]
   ["-j" "--json" "print results in JSON format"
    :id :json
    :default false]
   [nil "--github-api-token TOKEN" "GitHub API token"
    :id :github-api-token]
   [nil "--github-api-url URL" "GitHub API URL"
    :id :github-api-url
    :default "https://api.github.com"]
   [nil "--github-owner OWNER" "GitHub user or org name"
    :id :github-owner]
   [nil "--github-repo REPO" "GitHub repository name"
    :id :github-repo]
   [nil "--remote REMOTE" "remote"
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
      (try
        (cond
          (get options :help)
          (exit 0 (usage summary))
          (= subcommand "semver")
          (runsc
            (<! (subcommand/semver)))
          (= subcommand "semver-tag")
          (runsc
            (<! (subcommand/semver-tag)))
          (= subcommand "semver-tag-push")
          (runsc
            (<! (subcommand/semver-tag-push options)))
          (= subcommand "simple-semver")
          (runsc
            (<! (subcommand/simple-semver sargs)))
          (= subcommand "gh-release")
          (runsc
            (<! (subcommand/gh-release options)))
          :else
          (exit 1 (usage summary)))
        (catch :default e
          (if (:json options)
            (->> {:status 1
                  :error e}
                 (clj->js)
                 (.stringify js/JSON)
                 (exit 0))
            (exit 1 e)))))))

(defn -main []
  (run (.-argv nodejs/process)))

(set! *main-cli-fn* -main)
