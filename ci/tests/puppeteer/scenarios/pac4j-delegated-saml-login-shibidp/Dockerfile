FROM i2incommon/shib-idp:4.1.4_20210802

COPY shibcasauthn/no-conversation-state.jsp /opt/shibboleth-idp/edit-webapp
COPY shibcasauthn/cas-client-core-3.6.0.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib
COPY shibcasauthn/shib-cas-authenticator-4.0.0.jar /opt/shibboleth-idp/edit-webapp/WEB-INF/lib

COPY idp/web.xml /opt/shibboleth-idp/edit-webapp/WEB-INF
COPY idp/customidp.properties /opt/shibboleth-idp/conf
COPY idp/metadata-providers.xml /opt/shibboleth-idp/conf
COPY idp/attribute-filter.xml /opt/shibboleth-idp/conf
COPY idp/sp-metadata.xml /opt/shibboleth-idp/metadata

RUN ls /opt/shibboleth-idp && \
    chmod +x /opt/shibboleth-idp/bin/*.sh && \
    /opt/shibboleth-idp/bin/build.sh -Didp.target.dir="/opt/shibboleth-idp" && \
    sed -i 's/Location=\"https:\/\/idp.example.org/Location=\"https:\/\/localhost:9443/gi' /opt/shibboleth-idp/metadata/idp-metadata.xml && \
    sed -i 's/localhost:9443:8443/localhost:8443/gi' /opt/shibboleth-idp/metadata/idp-metadata.xml && \
    sed -i -E 's/validUntil=\".+T.+Z\"//i' /opt/shibboleth-idp/metadata/idp-metadata.xml && \
    cat /opt/shibboleth-idp/metadata/idp-metadata.xml
