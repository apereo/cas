FROM openjdk:11-jdk as overlay

MAINTAINER Apereo Foundation
MAINTAINER Misagh Moayyed

ARG CASVERSION
ARG CASBRANCH
ARG BOOTVERSION

RUN cd / \

RUN mkdir -p /cas-overlay/maven

COPY cas-overlay/maven/*.* /cas-overlay/maven/
COPY cas-overlay/mvnw /cas-overlay/mvnw
COPY cas-overlay/pom.xml /cas-overlay/pom.xml

RUN chmod +x cas-overlay/mvnw && chmod +x /opt/jre-home/bin/java;

EXPOSE 8080 8443

WORKDIR /cas-overlay

ENV JAVA_HOME /opt/jre-home
ENV PATH $PATH:$JAVA_HOME/bin:.
RUN ./mvnw clean package -Dcas.version=$CASVERSION  -Dspringboot.version=$BOOTVERSION

FROM openjdk:11-jdk as cas
COPY --from=overlay /cas-overlay/target/cas.war .
COPY thekeystore /etc/cas/

RUN export JAVA_HOME=/opt/jre-home \
    && export PATH=$PATH:$JAVA_HOME/bin:. \
    && cd /cas-overlay/ \
    && java -jar ./cas.war
