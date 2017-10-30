package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.services.AbstractServiceRegistryDao;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of the service registry interface which stores the services in a redis instance.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RedisServiceRegistryDao extends AbstractServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisServiceRegistryDao.class);
    private static final String CAS_SERVICE_PREFIX = RegisteredService.class.getSimpleName() + ':';

    private final RedisTemplate<String, RegisteredService> template;

    public RedisServiceRegistryDao(final RedisTemplate<String, RegisteredService> template) {
        this.template = template;
    }

    @Override
    public RegisteredService save(final RegisteredService rs) {
        try {
            final String redisKey = getRegisteredServiceRedisKey(rs);
            this.template.boundValueOps(redisKey).set(rs);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return rs;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        try {
            final String redisKey = getRegisteredServiceRedisKey(registeredService);
            this.template.delete(redisKey);
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public long size() {
        try {
            return this.template.keys(getPatternRegisteredServiceRedisKey()).size();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<RegisteredService> load() {
        try {
            return this.template.keys(getPatternRegisteredServiceRedisKey())
                    .stream()
                    .map(redisKey -> this.template.boundValueOps(redisKey).get())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new LinkedList<>();
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            final String redisKey = getRegisteredServiceRedisKey(id);
            return this.template.boundValueOps(redisKey).get();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        return load().stream().filter(r -> r.matches(id)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    private static String getRegisteredServiceRedisKey(final RegisteredService registeredService) {
        return getRegisteredServiceRedisKey(registeredService.getId());
    }

    private static String getRegisteredServiceRedisKey(final long id) {
        return CAS_SERVICE_PREFIX + id;
    }

    private static String getPatternRegisteredServiceRedisKey() {
        return CAS_SERVICE_PREFIX + "*";
    }

}
