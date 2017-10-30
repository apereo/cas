package org.apereo.cas.authentication.principal.cache;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.merger.IAttributeMerger;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.apereo.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.apereo.services.persondir.support.merger.ReplacingAttributeAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Parent class for retrieval principals attributes, provides operations
 * around caching, merging of attributes.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractPrincipalAttributesRepository implements PrincipalAttributesRepository, Closeable {
    /**
     * Default cache expiration time unit.
     */
    private static final String DEFAULT_CACHE_EXPIRATION_UNIT = TimeUnit.HOURS.name();

    /**
     * Default expiration lifetime based on the default time unit.
     */
    private static final long DEFAULT_CACHE_EXPIRATION_DURATION = 2;

    private static final long serialVersionUID = 6350245643948535906L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPrincipalAttributesRepository.class);

    /**
     * The expiration time.
     */
    protected long expiration;

    /**
     * Expiration time unit.
     */
    protected String timeUnit;

    /**
     * The merging strategy that deals with existing principal attributes
     * and those that are retrieved from the source. By default, existing attributes
     * are ignored and the source is always consulted.
     */
    protected MergingStrategy mergingStrategy;

    /**
     * Defines the merging strategy options.
     */
    public enum MergingStrategy {

        /**
         * Replace attributes.
         */
        REPLACE,
        /**
         * Add attributes.
         */
        ADD,
        /**
         * No merging.
         */
        NONE,
        /**
         * Multivalued attributes.
         */
        MULTIVALUED;

        /**
         * Get attribute merger.
         *
         * @return the attribute merger
         */
        public IAttributeMerger getAttributeMerger() {
            final String name = this.name().toUpperCase();

            switch (name.toUpperCase()) {
                case "REPLACE":
                    return new ReplacingAttributeAdder();
                case "ADD":
                    return new NoncollidingAttributeAdder();
                case "MULTIVALUED":
                    return new MultivaluedAttributeMerger();
                default:
                    return null;
            }
        }

    }

    private transient IPersonAttributeDao attributeRepository;


    /**
     * Instantiates a new principal attributes repository.
     * Simply used buy
     */
    protected AbstractPrincipalAttributesRepository() {
        this(DEFAULT_CACHE_EXPIRATION_DURATION, DEFAULT_CACHE_EXPIRATION_UNIT);
    }

    /**
     * Instantiates a new principal attributes repository.
     *
     * @param expiration the expiration
     * @param timeUnit   the time unit
     */
    public AbstractPrincipalAttributesRepository(final long expiration, final String timeUnit) {
        this.expiration = expiration;
        this.timeUnit = timeUnit;
    }

    /**
     * The merging strategy that deals with existing principal attributes
     * and those that are retrieved from the source. By default, existing attributes
     * are ignored and the source is always consulted.
     *
     * @param mergingStrategy the strategy to use for conflicts
     */
    public void setMergingStrategy(final MergingStrategy mergingStrategy) {
        this.mergingStrategy = mergingStrategy;
    }

    public MergingStrategy getMergingStrategy() {
        return this.mergingStrategy;
    }

    /**
     * Convert person attributes to principal attributes.
     *
     * @param attributes person attributes
     * @return principal attributes
     */
    protected Map<String, Object> convertPersonAttributesToPrincipalAttributes(
            final Map<String, List<Object>> attributes) {
        return attributes.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                    entry -> entry.getValue().size() == 1
                            ? entry.getValue().get(0) : entry.getValue(),
                    (e, f) -> f == null ? e : f));
    }

    /***
     * Convert principal attributes to person attributes.
     * @param p  the principal carrying attributes
     * @return person attributes
     */
    private static Map<String, List<Object>> convertPrincipalAttributesToPersonAttributes(final Principal p) {
        final Map<String, List<Object>> convertedAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        final Map<String, Object> principalAttributes = p.getAttributes();

        principalAttributes.entrySet().stream().forEach(entry -> {
            final Object values = entry.getValue();
            final String key = entry.getKey();
            if (values instanceof List) {
                convertedAttributes.put(key, (List) values);
            } else {
                convertedAttributes.put(key, CollectionUtils.wrap(values));
            }
        });
        return convertedAttributes;
    }

    /**
     * Obtains attributes first from the repository by calling
     * {@link org.apereo.services.persondir.IPersonAttributeDao#getPerson(String)}.
     *
     * @param id the person id to locate in the attribute repository
     * @return the map of attributes
     */
    protected Map<String, List<Object>> retrievePersonAttributesToPrincipalAttributes(final String id) {

        final IPersonAttributes attrs = getAttributeRepository().getPerson(id);

        if (attrs == null) {
            LOGGER.debug("Could not find principal [{}] in the repository so no attributes are returned.", id);
            return new HashMap<>(0);
        }

        final Map<String, List<Object>> attributes = attrs.getAttributes();
        if (attributes == null) {
            LOGGER.debug("Principal [{}] has no attributes and so none are returned.", id);
            return new HashMap<>(0);
        }
        return attributes;
    }

    @Override
    public Map<String, Object> getAttributes(final Principal p) {
        final Map<String, Object> cachedAttributes = getPrincipalAttributes(p);
        if (cachedAttributes != null && !cachedAttributes.isEmpty()) {
            LOGGER.debug("Found [{}] cached attributes for principal [{}] that are [{}]", cachedAttributes.size(), p.getId(),
                    cachedAttributes);
            return cachedAttributes;
        }

        if (getAttributeRepository() == null) {
            LOGGER.debug("No attribute repository is defined for [{}]. Returning default principal attributes for [{}]",
                    getClass().getName(), p.getId());
            return cachedAttributes;
        }

        final Map<String, List<Object>> sourceAttributes = retrievePersonAttributesToPrincipalAttributes(p.getId());
        LOGGER.debug("Found [{}] attributes for principal [{}] from the attribute repository.",
                sourceAttributes.size(), p.getId());

        if (this.mergingStrategy == null || this.mergingStrategy.getAttributeMerger() == null) {
            LOGGER.debug("No merging strategy found, so attributes retrieved from the repository will be used instead.");
            return convertAttributesToPrincipalAttributesAndCache(p, sourceAttributes);
        }

        final Map<String, List<Object>> principalAttributes = convertPrincipalAttributesToPersonAttributes(p);

        LOGGER.debug("Merging current principal attributes with that of the repository via strategy [{}]",
                this.mergingStrategy.getClass().getSimpleName());

        try {
            final Map<String, List<Object>> mergedAttributes =
                    this.mergingStrategy.getAttributeMerger().mergeAttributes(principalAttributes, sourceAttributes);
            return convertAttributesToPrincipalAttributesAndCache(p, mergedAttributes);
        } catch (final Exception e) {
            final StringBuilder builder = new StringBuilder();
            builder.append(e.getClass().getName().concat("-"));
            if (StringUtils.isNotBlank(e.getMessage())) {
                builder.append(e.getMessage());
            }

            LOGGER.error("The merging strategy [{}] for [{}] has failed to produce principal attributes because: [{}]. "
                            + "This usually is indicative of a bug and/or configuration mismatch. CAS will skip the merging process "
                            + "and will return the original collection of principal attributes [{}]",
                    this.mergingStrategy,
                    p.getId(),
                    builder.toString(),
                    principalAttributes);
            return convertAttributesToPrincipalAttributesAndCache(p, principalAttributes);
        }
    }

    /**
     * Convert attributes to principal attributes and cache.
     *
     * @param p                the p
     * @param sourceAttributes the source attributes
     * @return the map
     */
    private Map<String, Object> convertAttributesToPrincipalAttributesAndCache(final Principal p,
                                                                               final Map<String, List<Object>> sourceAttributes) {
        final Map<String, Object> finalAttributes = convertPersonAttributesToPrincipalAttributes(sourceAttributes);
        addPrincipalAttributes(p.getId(), finalAttributes);
        return finalAttributes;
    }

    /**
     * Add principal attributes into the underlying cache instance.
     *
     * @param id         identifier used by the cache as key.
     * @param attributes attributes to cache
     * @since 4.2
     */
    protected abstract void addPrincipalAttributes(String id, Map<String, Object> attributes);

    /**
     * Gets principal attributes from cache.
     *
     * @param p the principal
     * @return the principal attributes from cache
     */
    protected abstract Map<String, Object> getPrincipalAttributes(Principal p);

    public void setAttributeRepository(final IPersonAttributeDao attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    private IPersonAttributeDao getAttributeRepository() {
        try {
            if (this.attributeRepository == null) {
                final ApplicationContext context = ApplicationContextProvider.getApplicationContext();
                if (context != null) {
                    return context.getBean("attributeRepository", IPersonAttributeDao.class);
                }
                LOGGER.warn("No application context could be retrieved, so no attribute repository instance can be determined.");
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        
        return this.attributeRepository;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("mergingStrategy", this.mergingStrategy)
                .append("expiration", this.expiration)
                .append("timeUnit", this.timeUnit)
                .toString();
    }

    public long getExpiration() {
        return this.expiration;
    }

    public String getTimeUnit() {
        return this.timeUnit;
    }

    public void setTimeUnit(final String unit) {
        this.timeUnit = unit;
    }

    public void setExpiration(final long expiration) {
        this.expiration = expiration;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final AbstractPrincipalAttributesRepository rhs = (AbstractPrincipalAttributesRepository) obj;
        return new EqualsBuilder()
                .append(this.timeUnit, rhs.timeUnit)
                .append(this.expiration, rhs.expiration)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 133)
                .append(this.timeUnit)
                .append(this.expiration)
                .toHashCode();
    }
}
