package org.apereo.cas.adaptors.redis.services;

import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.support.events.service.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.support.events.service.CasRegisteredServiceSavedEvent;
import org.apereo.cas.util.LoggingUtils;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the service registry interface which stores the services in a redis instance.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString
public class RedisServiceRegistry extends AbstractServiceRegistry {

    private static final String CAS_SERVICE_PREFIX = RegisteredService.class.getSimpleName() + ':';

    private final RedisTemplate<String, RegisteredService> template;

    public RedisServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                final RedisTemplate<String, RegisteredService> template,
                                final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.template = template;
    }
                          
    @Override
    public RegisteredService save(final RegisteredService rs) {
        try {
            LOGGER.trace("Saving registered service [{}]", rs);
            val redisKey = getRegisteredServiceRedisKey(rs);
            invokeServiceRegistryListenerPreSave(rs);
            this.template.boundValueOps(redisKey).set(rs);
            LOGGER.trace("Saved registered service [{}]", rs);
            publishEvent(new CasRegisteredServiceSavedEvent(this, rs));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return rs;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        try {
            LOGGER.trace("Deleting registered service [{}]", registeredService);
            val redisKey = getRegisteredServiceRedisKey(registeredService);
            this.template.delete(redisKey);
            LOGGER.trace("Deleted registered service [{}]", registeredService);
            publishEvent(new CasRegisteredServiceDeletedEvent(this, registeredService));
            return true;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public void deleteAll() {
        getRegisteredServiceKeys().forEach(this.template::delete);

    }

    @Override
    public long size() {
        return getRegisteredServiceKeys().size();
    }

    @Override
    public Collection<RegisteredService> load() {
        val list = getRegisteredServiceKeys()
            .stream()
            .map(redisKey -> this.template.boundValueOps(redisKey).get())
            .filter(Objects::nonNull)
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        LOGGER.trace("Loaded registered services [{}]", list);
        list.forEach(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s)));
        return list;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        val redisKey = getRegisteredServiceRedisKey(id);
        val ops = this.template.boundValueOps(redisKey);
        LOGGER.trace("Locating service by identifier [{}] using key [{}]", id, redisKey);
        return ops.get();
    }

    private static String getRegisteredServiceRedisKey(final RegisteredService registeredService) {
        return getRegisteredServiceRedisKey(registeredService.getId());
    }

    private static String getRegisteredServiceRedisKey(final long id) {
        return CAS_SERVICE_PREFIX + id;
    }

    private static String getPatternRegisteredServiceRedisKey() {
        return CAS_SERVICE_PREFIX + '*';
    }

    private Set<String> getRegisteredServiceKeys() {
        return this.template.keys(getPatternRegisteredServiceRedisKey());
    }
}
