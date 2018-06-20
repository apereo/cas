FROM centos:centos7@sha256:eed5b251b615d1e70b10bcec578d64e8aa839d2785c2ffd5424e472818c42755

MAINTAINER Apereo Foundation
MAINTAINER Misagh Moayyed

ENV PATH=$PATH:$JRE_HOME/bin
ARG CASVERSION

RUN yum -y install wget tar unzip git \
    && yum -y clean all
    
# Download Azul Java, verify the hash, and install \
RUN set -x; \
    java_version=8.0.172; \
    zulu_version=8.30.0.1; \
    java_hash=0a101a592a177c1c7bc63738d7bc2930; \
    
    cd / \
    && wget http://cdn.azul.com/zulu/bin/zulu$zulu_version-jdk$java_version-linux_x64.tar.gz \
    && echo "$java_hash  zulu$zulu_version-jdk$java_version-linux_x64.tar.gz" | md5sum -c - \
    && tar -zxvf zulu$zulu_version-jdk$java_version-linux_x64.tar.gz -C /opt \
    && rm zulu$zulu_version-jdk$java_version-linux_x64.tar.gz \
    && ln -s /opt/zulu$zulu_version-jdk$java_version-linux_x64/jre/ /opt/jre-home;

RUN cd / \
	&& wget http://cdn.azul.com/zcek/bin/ZuluJCEPolicies.zip \
    && unzip ZuluJCEPolicies.zip \
    && mv -f ZuluJCEPolicies/*.jar /opt/jre-home/lib/security \
    && rm ZuluJCEPolicies.zip; 

# Set up Oracle Java properties
# RUN set -x; \
#     java_version=8u112; \
#     java_bnumber=15; \
#     java_semver=1.8.0_112; \
#     java_hash=eb51dc02c1607be94249dc28b0223be3712b618ef72f48d3e2bfd2645db8b91a; \

# # Download Oracle Java, verify the hash, and install \
#     cd / \
#     && wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" \
#     http://download.oracle.com/otn-pub/java/jdk/$java_version-b$java_bnumber/server-jre-$java_version-linux-x64.tar.gz \
#     && echo "$java_hash  server-jre-$java_version-linux-x64.tar.gz" | sha256sum -c - \
#     && tar -zxvf server-jre-$java_version-linux-x64.tar.gz -C /opt \
#     && rm server-jre-$java_version-linux-x64.tar.gz \
#     && ln -s /opt/jdk$java_semver/ /opt/jre-home;

COPY thekeystore /etc/cas/

RUN mkdir -p /cas-overlay/bin \
    && mkdir -p /cas-overlay/maven 

COPY cas-overlay/bin/*.* /cas-overlay/bin/
COPY cas-overlay/maven/*.* /cas-overlay/maven/
COPY cas-overlay/mvnw /cas-overlay/mvnw
COPY cas-overlay/pom.xml /cas-overlay/pom.xml

RUN chmod -R +x cas-overlay/bin \
    && chmod +x cas-overlay/mvnw \
    && chmod +x /opt/jre-home/bin/java;

# Enable if you are using Oracle Java
#	&& chmod 750 /opt/jre-home/jre/bin/java;

EXPOSE 8080 8443

WORKDIR /cas-overlay

ENV JAVA_HOME /opt/jre-home
ENV PATH $PATH:$JAVA_HOME/bin:.
RUN ./mvnw clean package -T 10 -Dcas.version=$CASVERSION

CMD ["bin/run-cas.sh"]
