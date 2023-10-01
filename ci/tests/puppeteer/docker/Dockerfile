ARG JAVA_VERSION
FROM azul/zulu-openjdk:${JAVA_VERSION}

ARG SCENARIO_FOLDER
ARG SCENARIO_PATH
ARG SCENARIO

ENV RUN_ARGS=""
ENV CAS_PROPERTIES=""
ENV SERVER_PORT="8443"

RUN cd / \
    && mkdir -p /etc/cas/config \
    && mkdir -p /etc/cas/services \
    && mkdir -p /etc/cas/saml \
    && mkdir -p cas-server;

RUN \
    if [ -d "$SCENARIO_FOLDER/services" ]; then \
        cp -r $SCENARIO_FOLDER/services /etc/cas/services; \
    fi;
    
COPY cas.war cas-server/
COPY thekeystore /etc/cas

COPY entrypoint.sh cas-server/
RUN chmod +x /cas-server/entrypoint.sh

EXPOSE 8080 $SERVER_PORT 5005

ENV PATH $PATH:$JAVA_HOME/bin:.
WORKDIR cas-server
ENTRYPOINT ["/cas-server/entrypoint.sh"]
