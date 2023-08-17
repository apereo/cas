ARG redisVersion

FROM redis/redis-stack:7.2.0-v0 as redisearch
FROM bitnami/redis:$redisVersion

COPY --from=redisearch /opt/redis-stack/lib/redisearch.so /usr/lib/redis/modules/redisearch.so
COPY --from=redisearch /opt/redis-stack/lib/rejson.so /usr/lib/redis/modules/rejson.so

CMD ["/run.sh", "--loadmodule", "/usr/lib/redis/modules/redisearch.so", "--loadmodule", "/usr/lib/redis/modules/rejson.so"]
