ARG redisVersion

FROM redis/redis-stack:7.2.0-v0 as redisearch
FROM bitnami/redis:$redisVersion
USER root
RUN mkdir -p /var/redis/modules
COPY --from=redisearch /opt/redis-stack/lib/redisearch.so /var/redis/modules/redisearch.so
COPY --from=redisearch /opt/redis-stack/lib/rejson.so /var/redis/modules/rejson.so
USER 1001
RUN ls /var/redis/modules
CMD ["/run.sh", "--loadmodule", "/var/redis/modules/redisearch.so", "--loadmodule", "/var/redis/modules/rejson.so"]

