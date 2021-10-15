# syntax=docker/dockerfile:1

FROM mozilla/sbt:8u292_1.5.4 AS SBT_BUILD

WORKDIR /nsi-requester
COPY . .
RUN sbt stage

FROM openjdk:8-jre-alpine3.9

WORKDIR /nsi-requester
RUN apk update
RUN apk add bash
COPY --from=SBT_BUILD /nsi-requester/target/universal/stage/. .

ENV PORT=9000
ENV ADDRESS="0.0.0.0"
ENV CONFIG=/config-overrides.conf
#ENV EXTRA="-J-Xms512m -J-Xmx512m -J-server -J-verbose:gc -J-XX:+PrintGCDetails -J-XX:+PrintGCDateStamps -J-Xloggc:./nsi-safnari/logs/gc.log -J-XX:+UseGCLogFileRotation -J-XX:NumberOfGCLogFiles=10 -J-XX:GCLogFileSize=10M -J-XX:+UseParallelGC -J-XX:+UseParallelOldGC"
ENV EXTRA="-J-Xms512m -J-Xmx512m -J-server -J-verbose:gc -J-XX:+PrintGCDetails -J-XX:+PrintGCDateStamps -J-XX:+UseParallelGC -J-XX:+UseParallelOldGC"

EXPOSE 9000/tcp
CMD bin/nsi-requester -Dconfig.file=$CONFIG -Dhttp.port=$PORT -Dhttp.address=$ADDRESS -DapplyEvolutions.default=true $EXTRA
