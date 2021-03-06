# orchis

[![CircleCI](https://circleci.com/gh/rinx/orchis/tree/master.svg?style=svg)](https://circleci.com/gh/rinx/orchis)
[![Docker Pulls](https://img.shields.io/docker/pulls/rinx/orchis.svg?style=flat-square)](https://hub.docker.com/r/rinx/orchis)
[![GitHub Actions: Build docker image](https://github.com/rinx/orchis/workflows/Build%20docker%20image/badge.svg)](https://github.com/rinx/orchis/actions)

Orchis has various types of _weapons_ for CI/CD platforms.

It is named after [GP03](https://gundam.fandom.com/wiki/RX-78GP03_Gundam_%22Dendrobium%22)'s armed base.

## Requirements

- `git`

## Usage

    $ orchis semver
    $ orchis simple-semver patch
    $ orchis gh-release
    $ orchis run step1 -f orchis.edn # not implemented yet

or using docker image [rinx/orchis](https://hub.docker.com/r/rinx/orchis),

    $ docker run -v `pwd`:/src -it rinx/orchis simple-semver patch
    $ docker run -v `pwd`:/src -v ${SSH_AUTH_SOCK}:/ssh-agent -e SSH_AUTH_SOCK=/ssh-agent -it rinx/orchis semver-tag-push

### steps

#### builtin steps

- semver
- semver-tag
- semver-tag-push
- simple-semver
- GitHub release

builtin steps are written in ClojureScript.

##### semver

It returns the bumped version string along with contexts of the commit comments automatically.
It is realized by regex matching for commit comments.
If the comment contains "[patch]" or "[PATCH]" strings, it gives incremented version (eg. 0.0.1 -> 0.0.2).
Also, "[minor]" or "[MINOR]" for minor updates, "[major]" or "[MAJOR]" for major updates are supported.
On the future work, it will be customizable by given CLI options.

##### semver-tag

Same as `semver`, it will bump git tag.

##### semver-tag-push

Same as `semver-tag`, it will push bumped tags to remote.

##### simple-semver

It returns incremented version string you specified.

eg.

    $ orchis simple-semver patch
    $ orchis simple-semver minor
    $ orchis simple-semver major

##### GitHub release (`gh-release`)

It creates a new GitHub release from the latest tag.

#### step configurations

**not implemented yet.**

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
    user=> (dev)
    ClojureScript 1.10.439
    cljs.user=>
