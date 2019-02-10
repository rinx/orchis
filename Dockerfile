FROM clojure:lein-alpine AS base

RUN apk update \
    && apk add \
    nodejs \
    nodejs-npm

RUN mkdir -p /root/orchis

WORKDIR /root/orchis

COPY . .

RUN lein pkg

FROM ubuntu:devel

RUN apt-get update \
    && apt-get install -y git

COPY --from=base /root/orchis/orchis /root/orchis

RUN mkdir -p /src
WORKDIR /src

ENTRYPOINT ["/root/orchis"]
