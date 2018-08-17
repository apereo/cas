FROM openjdk:11-jdk-slim as overlay

MAINTAINER Apereo Foundation
MAINTAINER Misagh Moayyed

ARG CASVERSION
ARG BOOTVERSION

RUN cd / \
    && mkdir -p /cas-overlay/maven
    && cp -R cas-overlay/maven/*.* /cas-overlay/maven/ \
    && cp -R cas-overlay/mvnw /cas-overlay/mvnw \
    && cp -R cas-overlay/pom.xml /cas-overlay/pom.xml \
    && chmod +x cas-overlay/mvnw

WORKDIR /cas-overlay
RUN ./mvnw clean package -T 10 -Dcas.version=$CASVERSION -Dspringboot.version=$BOOTVERSION

FROM openjdk:11-jdk-slim as cas
EXPOSE 8080 8443
COPY --from=overlay /cas-overlay/target/cas.war .
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
