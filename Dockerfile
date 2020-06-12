FROM adoptopenjdk/openjdk11:alpine-slim as jar
RUN wget --no-check-certificate -O build-cache.jar https://docs.gradle.com/build-cache-node/jar/build-cache-node-9.1.jar

FROM adoptopenjdk/openjdk11:alpine-slim
COPY --from=jar build-cache.jar .
RUN java -jar ./build-cache.jar --port $PORT