FROM gradle/build-cache-node:latest

ENTRYPOINT ["build-cache-node", "--port $PORT"]