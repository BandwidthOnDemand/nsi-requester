# syntax=docker/dockerfile:1
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-21.0.2_13_1.10.2_3.5.1 AS build
RUN apt-get update && apt-get install -y nodejs
WORKDIR /usr/local/src/nsi-requester
COPY LICENSE build.sbt .
COPY .git/ .git/
COPY app/ app/
COPY conf/ conf/
COPY project/ project/
COPY public/ public/
RUN --mount=type=secret,id=github_token <<EOF
set -e
export GITHUB_TOKEN="$(cat /run/secrets/github_token)"
sbt packageZipTarball
EOF

FROM eclipse-temurin:21

ENV PORT=9000
ENV ADDRESS="0.0.0.0"
ENV CONFIG=/config-overrides.conf
#ENV EXTRA="-J-Xms512m -J-Xmx512m -J-server -J-verbose:gc -J-XX:+PrintGCDetails -J-XX:+PrintGCDateStamps -J-Xloggc:./nsi-requester/logs/gc.log -J-XX:+UseGCLogFileRotation -J-XX:NumberOfGCLogFiles=10 -J-XX:GCLogFileSize=10M -J-XX:+UseParallelGC -J-XX:+UseParallelOldGC"
ENV EXTRA="-J-Xms512m -J-Xmx512m -J-server -J-verbose:gc -J-XX:+PrintGCDetails -J-XX:+PrintGCDateStamps -J-XX:+UseParallelGC -J-XX:+UseParallelOldGC"

WORKDIR /nsi-requester
COPY --from=build /usr/local/src/nsi-requester/target/universal/*.tgz nsi-requester.tgz
RUN tar xvzf nsi-requester.tgz --strip-components=1 && rm nsi-requester.tgz

EXPOSE 9000/tcp
CMD bin/nsi-requester -Dconfig.file=$CONFIG -Dhttp.port=$PORT -Dhttp.address=$ADDRESS -DapplyEvolutions.default=true $EXTRA
