package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.annotation.JsonCreator;
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

        val cachedAttributes = fetchCachedPrincipalAttributes(principal, registeredService);
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
            val mergedAttributes = CoreAuthenticationUtils.getAttributeMerger(mergeStrategy)
                .mergeAttributes(principalAttributes, personDirectoryAttributes);
            return convertAttributesToPrincipalAttributesAndCache(principal, mergedAttributes, registeredService);
        }
        return convertAttributesToPrincipalAttributesAndCache(principal, principalAttributes, registeredService);
    }

    @Override
    public void update(final String id, final Map<String, List<Object>> attributes,
                       final RegisteredService registeredService) {
        ApplicationContextProvider.getPrincipalAttributesRepositoryCache()
            .ifPresent(cache -> {
                cache.putAttributes(registeredService, this, id, attributes);
                LOGGER.trace("Cached attributes for [{}] and [{}]", id, registeredService.getName());
            });
    }

    /**
     * Gets cached principal attributes.
     *
     * @param principal         the principal
     * @param registeredService the registered service
     * @return the cached principal attributes
     */
    protected Map<String, List<Object>> fetchCachedPrincipalAttributes(final Principal principal, final RegisteredService registeredService) {
        return ApplicationContextProvider.getPrincipalAttributesRepositoryCache()
            .map(cache -> cache.fetchAttributes(registeredService, this, principal))
            .orElseGet(() -> new HashMap<>(0));
    }
}
