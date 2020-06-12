FROM gradle/build-cache-node:latest

ENTRYPOINT ["--port $PORT"]