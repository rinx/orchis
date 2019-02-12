FROM clojure:lein-alpine AS base

RUN apk update \
    && apk add \
    nodejs \
    nodejs-npm \
    git \
    musl-dev \
    libstdc++

RUN mkdir -p /root/orchis

WORKDIR /root/orchis

COPY . .

RUN lein pkg-alpine

FROM scratch

LABEL maintainer "Rintaro Okamura <rintaro.okamura@gmail.com>"

COPY --from=base /usr/bin/git /bin/git
COPY --from=base /root/orchis/orchis /bin/orchis

COPY --from=base /lib/ld-musl-x86_64.so.1 /lib/ld-musl-x86_64.so.1
COPY --from=base /usr/lib/libstdc++.so.6 /usr/lib/libstdc++.so.6
COPY --from=base /usr/lib/libgcc_s.so.1 /usr/lib/libgcc_s.so.1
COPY --from=base /usr/lib/libpcre2-8.so.0 /usr/lib/libpcre2-8.so.0
COPY --from=base /lib/libz.so.1 /lib/libz.so.1

ENV PATH /bin

WORKDIR /src

ENTRYPOINT ["/bin/orchis"]
