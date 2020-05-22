package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Wrapper around an attribute repository where attributes cached for a configurable period
 * based on google guava's caching library.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@ToString(callSuper = true)
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, of = {"timeUnit", "expiration"})
public class CachingPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
    private static final long serialVersionUID = 6350244643948535906L;

    /**
     * The expiration time.
     */
    protected long expiration;

    /**
     * Expiration time unit.
     */
    protected String timeUnit;


    @JsonCreator
    public CachingPrincipalAttributesRepository(@JsonProperty("timeUnit") final String timeUnit,
                                                @JsonProperty("expiration") final long expiryDuration) {
        this.timeUnit = timeUnit;
        this.expiration = expiryDuration;
    }

    @Override
    public Map<String, List<Object>> getAttributes(final Principal principal, final RegisteredService registeredService) {
        val mergeStrategy = determineMergingStrategy();
        LOGGER.trace("Determined merging strategy as [{}]", mergeStrategy);

        val cachedAttributes = getCachedPrincipalAttributes(principal, registeredService);
        if (cachedAttributes != null && !cachedAttributes.isEmpty()) {
            LOGGER.debug("Found [{}] cached attributes for principal [{}] that are [{}]", cachedAttributes.size(), principal.getId(), cachedAttributes);
            return cachedAttributes;
        }

        val principalAttributes = getPrincipalAttributes(principal);
        LOGGER.trace("Principal attributes extracted for [{}] are [{}]", principal.getId(), principalAttributes);

        if (areAttributeRepositoryIdsDefined()) {
            val personDirectoryAttributes = retrievePersonAttributesFromAttributeRepository(principal);
            LOGGER.debug("Found [{}] attributes for principal [{}] from the attribute repository.", personDirectoryAttributes.size(), principal.getId());

            LOGGER.debug("Merging current principal attributes with that of the repository via strategy [{}]", mergeStrategy);
            val mergedAttributes = mergeStrategy.getAttributeMerger().mergeAttributes(principalAttributes, personDirectoryAttributes);
            return convertAttributesToPrincipalAttributesAndCache(principal, mergedAttributes, registeredService);
        }
        return convertAttributesToPrincipalAttributesAndCache(principal, principalAttributes, registeredService);
    }

    @Override
    protected void addPrincipalAttributes(final String id, final Map<String, List<Object>> attributes,
                                          final RegisteredService registeredService) {
        try {
            val cache = getCacheInstanceFromApplicationContext();
            cache.putCachedAttributesFor(registeredService, this, id, attributes);
            LOGGER.trace("Cached attributes for [{}]", id);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }

    /**
     * Gets cached principal attributes.
     *
     * @param principal         the principal
     * @param registeredService the registered service
     * @return the cached principal attributes
     */
    @JsonIgnore
    protected Map<String, List<Object>> getCachedPrincipalAttributes(final Principal principal, final RegisteredService registeredService) {
        try {
            val cache = getCacheInstanceFromApplicationContext();
            return cache.getCachedAttributesFor(registeredService, this, principal);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return new HashMap<>(0);
    }

    /**
     * Gets cache instance from application context.
     *
     * @return the cache instance from application context
     */
    @JsonIgnore
    public static PrincipalAttributesRepositoryCache getCacheInstanceFromApplicationContext() {
        val ctx = ApplicationContextProvider.getApplicationContext();
        return Objects.requireNonNull(ctx)
            .getBean("principalAttributesRepositoryCache", PrincipalAttributesRepositoryCache.class);
    }

}
