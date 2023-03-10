ARG redisVersion

FROM docker.io/redislabs/redisearch:latest as redisearch
FROM bitnami/redis:$redisVersion

COPY --from=redisearch /usr/lib/redis/modules/redisearch.so /usr/lib/redis/modules/redisearch.so
COPY --from=redisearch /usr/lib/redis/modules/rejson.so /usr/lib/redis/modules/rejson.so

CMD ["/run.sh", "--loadmodule", "/usr/lib/redis/modules/redisearch.so", "--loadmodule", "/usr/lib/redis/modules/rejson.so"]
