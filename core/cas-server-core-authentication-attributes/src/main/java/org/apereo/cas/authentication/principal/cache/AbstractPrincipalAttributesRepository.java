package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.AttributeMergingStrategy;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apereo.services.persondir.IPersonAttributeDao;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Parent class for retrieval principals attributes, provides operations
 * around caching, merging of attributes.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@ToString(exclude = "lock")
@NoArgsConstructor
@EqualsAndHashCode(of = {"mergingStrategy", "attributeRepositoryIds"})
public abstract class AbstractPrincipalAttributesRepository implements RegisteredServicePrincipalAttributesRepository, AutoCloseable {
    private static final long serialVersionUID = 6350245643948535906L;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private final transient Object lock = new Object();

    /**
     * The merging strategy that deals with existing principal attributes
     * and those that are retrieved from the source. By default, existing attributes
     * are ignored and the source is always consulted.
     */
    @Getter
    @Setter
    private AttributeMergingStrategy mergingStrategy = AttributeMergingStrategy.MULTIVALUED;

    @Getter
    @Setter
    private Set<String> attributeRepositoryIds = new LinkedHashSet<>(0);

    @Getter
    @Setter
    private boolean ignoreResolvedAttributes;

    @Override
    public abstract Map<String, List<Object>> getAttributes(Principal principal, RegisteredService registeredService);

    @Override
    public void close() {
    }

    /**
     * Gets attribute repository.
     *
     * @return the attribute repository
     */
    @JsonIgnore
    protected static IPersonAttributeDao getAttributeRepository() {
        val repositories = ApplicationContextProvider.getAttributeRepository();
        return repositories.orElse(null);
    }

    /***
     * Convert principal attributes to person attributes.
     * @param attributes the attributes
     * @return person attributes
     */
    protected static Map<String, List<Object>> convertPrincipalAttributesToPersonAttributes(final Map<String, ?> attributes) {
        val convertedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        val principalAttributes = new LinkedHashMap<>(attributes);
        principalAttributes.forEach((key, values) -> {
            if (values instanceof Collection) {
                val uniqueValues = new LinkedHashSet<Object>(Collection.class.cast(values));
                val listedValues = new ArrayList<Object>(uniqueValues);
                convertedAttributes.put(key, listedValues);
            } else {
                convertedAttributes.put(key, CollectionUtils.wrap(values));
            }
        });
        return convertedAttributes;
    }

    /**
     * Convert person attributes to principal attributes.
     *
     * @param attributes person attributes
     * @return principal attributes
     */
    protected static Map<String, List<Object>> convertPersonAttributesToPrincipalAttributes(final Map<String, List<Object>> attributes) {
        return attributes.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Convert attributes to principal attributes and cache.
     *
     * @param principal         the principal
     * @param sourceAttributes  the source attributes
     * @param registeredService the registered service
     * @return the map
     */
    protected Map<String, List<Object>> convertAttributesToPrincipalAttributesAndCache(final Principal principal,
                                                                                       final Map<String, List<Object>> sourceAttributes,
                                                                                       final RegisteredService registeredService) {
        val finalAttributes = convertPersonAttributesToPrincipalAttributes(sourceAttributes);
        addPrincipalAttributes(principal.getId(), finalAttributes, registeredService);
        return finalAttributes;
    }

    /**
     * Add principal attributes into the underlying cache instance.
     *
     * @param id                identifier used by the cache as key.
     * @param attributes        attributes to cache
     * @param registeredService the registered service
     * @since 4.2
     */
    protected abstract void addPrincipalAttributes(String id, Map<String, List<Object>> attributes, RegisteredService registeredService);

    /**
     * Calculate merging strategy attribute merging strategy.
     *
     * @return the attribute merging strategy
     */
    protected AttributeMergingStrategy determineMergingStrategy() {
        return ObjectUtils.defaultIfNull(getMergingStrategy(), AttributeMergingStrategy.MULTIVALUED);
    }

    /**
     * Are attribute repository ids defined boolean.
     *
     * @return true/false
     */
    @JsonIgnore
    protected boolean areAttributeRepositoryIdsDefined() {
        return attributeRepositoryIds != null && !attributeRepositoryIds.isEmpty();
    }

    /**
     * Obtains attributes first from the repository by calling
     * {@link IPersonAttributeDao#getPerson(String, org.apereo.services.persondir.IPersonAttributeDaoFilter)}.
     *
     * @param principal the person to locate in the attribute repository
     * @return the map of attributes
     */
    protected Map<String, List<Object>> retrievePersonAttributesFromAttributeRepository(final Principal principal) {
        synchronized (lock) {
            val repository = getAttributeRepository();
            if (repository == null) {
                LOGGER.warn("No attribute repositories could be fetched from application context");
                return new HashMap<>(0);
            }

            return PrincipalAttributeRepositoryFetcher.builder()
                .attributeRepository(repository)
                .principalId(principal.getId())
                .activeAttributeRepositoryIdentifiers(this.attributeRepositoryIds)
                .currentPrincipal(principal)
                .build()
                .retrieve();
        }
    }

    /**
     * Gets principal attributes.
     *
     * @param principal the principal
     * @return the principal attributes
     */
    @JsonIgnore
    protected Map<String, List<Object>> getPrincipalAttributes(final Principal principal) {
        if (ignoreResolvedAttributes) {
            return new HashMap<>(0);
        }
        return convertPrincipalAttributesToPersonAttributes(principal.getAttributes());
    }
}
