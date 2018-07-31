FROM openjdk:11-jdk-slim as overlay

MAINTAINER Apereo Foundation
MAINTAINER Misagh Moayyed

ARG CASVERSION
ARG BOOTVERSION

RUN cd / && mkdir -p /cas-overlay/maven

COPY cas-overlay/maven/*.* /cas-overlay/maven/
COPY cas-overlay/mvnw /cas-overlay/mvnw
COPY cas-overlay/pom.xml /cas-overlay/pom.xml

RUN chmod +x cas-overlay/mvnw

EXPOSE 8080 8443

WORKDIR /cas-overlay

RUN ./mvnw clean package -T 10 -Dcas.version=$CASVERSION -Dspringboot.version=$BOOTVERSION

FROM openjdk:11-jdk-slim as cas
COPY --from=overlay /cas-overlay/target/cas.war .
COPY thekeystore /etc/cas/
