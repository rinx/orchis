# orchis

Orchis is a tool for storing various types of "weapons" for CI/CD platforms.
It is named after [GP03](https://gundam.fandom.com/wiki/RX-78GP03_Gundam_%22Dendrobium%22)'s armed base.


## Usage

    $ orchis run semver
    $ orchis run gh-release
    $ orchis run step1 -f orchis.edn


### steps

#### builtin steps

- semver
- GitHub release

builtin steps are written in ClojureScript or ShellScript.


#### step configurations

Create `orchis.edn` file in your repository.

```clojure
{:steps
 {:step1
  {:description "git commit"
   :enabled true
   :condition [[:branch "master" "develop"]]
   :tasks [["git" "commit" "-m" "'abc'"]
           ["git" "push" "origin" "master"]]}
  :step2
  {:description "deploy"
   :enabled false
   :tasks [["cf" "deploy"]]}
  :step3
  {:description "combination"
   :enabled true
   :tasks [["git" "add"]
           [:step :step1]
           [:builtin :semver]
           [:builtin :github-release]]}}}
```


### target platforms

- Google Cloud Build
- CircleCI
- Drone.io
- Screwdriver.cd


## Packaging using `pkg`

    $ lein pkg


## Development

To install dependencies,

    $ lein npm install

To build,

    $ lein build
    $ lein build-auto

To run cljs repl,

    $ lein trampoline noderepl

or

    $ lein repl
    user=> (require '[cljs.repl :as repl] '[cljs.repl.node :as node])
    user=> (repl/repl (node/repl-env))
    ClojureScript 1.10.439
    cljs.user=>

