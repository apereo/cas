FROM gradle/build-cache-node:latest

# CMD "java -jar ./build-cache-node.jar --port $PORT"
ENTRYPOINT ["java", "-jar", "build-cache-node.jar", "--port $PORT"]

