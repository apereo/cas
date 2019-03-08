package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around an attribute repository where attributes cached for a configurable period
 * based on google guava's caching library.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@ToString(of = {"timeUnit", "expiration"}, callSuper = true)
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, of = {"timeUnit", "expiration"})
public class CachingPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository implements Closeable {
    private static final long serialVersionUID = 6350244643948535906L;

    private static final int DEFAULT_MAXIMUM_CACHE_SIZE = 1000;
    private static final String DEFAULT_CACHE_EXPIRATION_UNIT = TimeUnit.HOURS.name();

    /**
     * The expiration time.
     */
    @Getter
    @Setter
    protected long expiration;

    /**
     * Expiration time unit.
     */
    @Getter
    @Setter
    protected String timeUnit;

    @JsonIgnore
    @javax.persistence.Transient
    @Transient
    private transient Cache<String, Map<String, Object>> cache;

    @JsonIgnore
    private long maxCacheSize = DEFAULT_MAXIMUM_CACHE_SIZE;

    /**
     * Instantiates a new caching attributes principal factory.
     * Sets the default cache size to {@link #DEFAULT_MAXIMUM_CACHE_SIZE}.
     *
     * @param timeUnit       the time unit
     * @param expiryDuration the expiry duration
     */
    @JsonCreator
    public CachingPrincipalAttributesRepository(@JsonProperty("timeUnit") final String timeUnit, @JsonProperty("expiration") final long expiryDuration) {
        this(DEFAULT_MAXIMUM_CACHE_SIZE, timeUnit, expiryDuration);
    }

    public CachingPrincipalAttributesRepository(final long maxCacheSize,
                                                final String timeUnit,
                                                final long expiryDuration) {
        this.maxCacheSize = maxCacheSize;
        this.timeUnit = timeUnit;
        this.expiration = expiryDuration;

        initializeCacheIfNecessary();
    }

    @Override
    public Map<String, Object> getAttributes(final Principal principal, final RegisteredService registeredService) {
        val mergeStrategy = determineMergingStrategy();

        val cachedAttributes = getCachedPrincipalAttributes(principal);
        if (cachedAttributes != null && !cachedAttributes.isEmpty()) {
            LOGGER.debug("Found [{}] cached attributes for principal [{}] that are [{}]", cachedAttributes.size(), principal.getId(), cachedAttributes);
            return cachedAttributes;
        }

        val principalAttributes = getPrincipalAttributes(principal);

        if (areAttributeRepositoryIdsDefined()) {
            val personDirectoryAttributes = retrievePersonAttributesFromAttributeRepository(principal.getId());
            LOGGER.debug("Found [{}] attributes for principal [{}] from the attribute repository.", personDirectoryAttributes.size(), principal.getId());

            LOGGER.debug("Merging current principal attributes with that of the repository via strategy [{}]", mergeStrategy);
            val mergedAttributes = mergeStrategy.getAttributeMerger().mergeAttributes(principalAttributes, personDirectoryAttributes);
            return convertAttributesToPrincipalAttributesAndCache(principal, mergedAttributes);
        }
        return convertAttributesToPrincipalAttributesAndCache(principal, principalAttributes);
    }

    @Override
    protected void addPrincipalAttributes(final String id, final Map<String, Object> attributes) {
        initializeCacheIfNecessary();

        this.cache.put(id, attributes);
        LOGGER.debug("Cached attributes for [{}]", id);
    }

    /**
     * Gets cached principal attributes.
     *
     * @param p the principal
     * @return the cached principal attributes
     */
    @JsonIgnore
    protected Map<String, Object> getCachedPrincipalAttributes(final Principal p) {
        try {
            initializeCacheIfNecessary();

            return this.cache.get(p.getId(), s -> {
                LOGGER.debug("No cached attributes could be found for [{}]", p.getId());
                return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>(0);
    }

    @Override
    public void close() {
        if (this.cache != null) {
            this.cache.cleanUp();
        }
    }

    private void initializeCacheIfNecessary() {
        if (this.cache == null) {
            this.cache = Caffeine.newBuilder()
                .initialCapacity(DEFAULT_MAXIMUM_CACHE_SIZE)
                .maximumSize(this.maxCacheSize <= 0 ? DEFAULT_MAXIMUM_CACHE_SIZE : this.maxCacheSize)
                .expireAfterWrite(this.expiration, TimeUnit.valueOf(StringUtils.defaultString(this.timeUnit, DEFAULT_CACHE_EXPIRATION_UNIT)))
                .build(s -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
        }
    }
}
