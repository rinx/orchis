(defproject orchis "0.1.0-SNAPSHOT"
  :description "FIXME"
  :url "https://github.com/YOURNAME/orchis"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :clean-targets ["build" :target-path]

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.439"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [org.clojure/core.async "0.4.490"]
                 [io.nervous/cljs-nodejs-externs "0.2.0"]
                 [org.clojure/tools.cli "0.4.1"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-npm "0.6.2"]
            [org.bodil/lein-noderepl "0.1.11"]]

  :npm {:dependencies [[source-map-support "0.4.0"]
                       [simple-git "1.107.0"]]
        :devDependencies [[pkg "4.3.7"]]
        :package {:scripts {:pkg "pkg -t node10-linux-x64 -c package-lock.json build/main.js"
                            :pkg-win "pkg -t node10-win-x64 -c package-lock.json build/main.js"
                            :pkg-mac "pkg -t node10-macos-x64 -c package-lock.json build/main.js"}}}

  :aliases {"build" ["cljsbuild" "once" "main"]
            "build-auto" ["cljsbuild" "auto" "main"]
            "pkg" ["do"
                   ["npm" "install"]
                   "build"
                   ["npm" "run" "pkg"]]}

  :cljsbuild {:builds [{:id "main"
                        :source-paths ["src"]
                        :compiler {:output-to "build/main.js"
                                   :output-dir "build/js"
                                   :optimizations :advanced
                                   :target :nodejs
                                   :source-map "build/main.js.map"}}]}
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["dev"]}})
