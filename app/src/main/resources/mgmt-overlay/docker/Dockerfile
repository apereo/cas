FROM adoptopenjdk/openjdk11:alpine-slim AS management-overlay

RUN mkdir -p cas-management-overlay
#COPY ./src cas-management-overlay/src/
COPY ./gradle/ cas-management-overlay/gradle/
COPY ./gradlew ./settings.gradle ./build.gradle ./gradle.properties /cas-management-overlay/

RUN mkdir -p ~/.gradle \
    && echo "org.gradle.daemon=false" >> ~/.gradle/gradle.properties \
    && echo "org.gradle.configureondemand=true" >> ~/.gradle/gradle.properties \
    && cd cas-management-overlay \
    && chmod 750 ./gradlew \
    && ./gradlew --version;

RUN cd cas-management-overlay \
    && ./gradlew clean build --parallel --no-daemon;


FROM adoptopenjdk/openjdk11:alpine-jre AS cas-management

LABEL "Organization"="Apereo"
LABEL "Description"="Apereo CAS Management"

RUN cd / \
    && mkdir -p /etc/cas/config \
    && mkdir -p /etc/cas/services \
    && mkdir -p cas-management-overlay;

COPY --from=management-overlay cas-management-overlay/build/libs/cas-management.war cas-management-overlay/
COPY etc/cas/ /etc/cas/
COPY etc/cas/config/ /etc/cas/config/
#COPY etc/cas/services/ /etc/cas/services/

EXPOSE 8080 8443

ENV PATH $PATH:$JAVA_HOME/bin:.

WORKDIR cas-management-overlay
ENTRYPOINT ["java", "-server", "-noverify", "-Xmx2048M", "-jar", "cas-management.war"]
