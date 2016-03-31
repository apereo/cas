package org.jasig.cas.authentication.principal.cache;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalAttributesRepository;
import org.jasig.cas.util.ApplicationContextProvider;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.jasig.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.jasig.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.jasig.services.persondir.support.merger.ReplacingAttributeAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Parent class for retrieval principals attributes, provides operations
 * around caching, merging of attributes.
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractPrincipalAttributesRepository implements PrincipalAttributesRepository, Closeable {
    /** Default cache expiration time unit. */
    private static final TimeUnit DEFAULT_CACHE_EXPIRATION_UNIT = TimeUnit.HOURS;

    /** Default expiration lifetime based on the default time unit. */
    private static final long DEFAULT_CACHE_EXPIRATION_DURATION = 2;

    private static final long serialVersionUID = 6350245643948535906L;

    /** Logger instance. */
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    /** The expiration time. */
    protected final long expiration;

    /** Expiration time unit. */
    protected final TimeUnit timeUnit;

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

        /** Replace attributes. */
        REPLACE,
        /** Add attributes. */
        ADD,
        /** No merging. */
        NONE,
        /** Multivalued attributes. */
        MULTIVALUED;

        /**
         * Get attribute merger.
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
     * @param expiration the expiration
     * @param timeUnit the time unit
     */
    public AbstractPrincipalAttributesRepository(final long expiration, final TimeUnit timeUnit) {
        this.expiration = expiration;
        this.timeUnit = timeUnit;
    }

    /**
     * The merging strategy that deals with existing principal attributes
     * and those that are retrieved from the source. By default, existing attributes
     * are ignored and the source is always consulted.
     * @param mergingStrategy the strategy to use for conflicts
     */
    public final void setMergingStrategy(final MergingStrategy mergingStrategy) {
        this.mergingStrategy = mergingStrategy;
    }

    public final MergingStrategy getMergingStrategy() {
        return mergingStrategy;
    }

    /**
     * Convert person attributes to principal attributes.
     * @param attributes person attributes
     * @return principal attributes
     */
    protected final Map<String, Object> convertPersonAttributesToPrincipalAttributes(
            final Map<String, List<Object>> attributes) {
        final Map<String, Object> convertedAttributes = new HashMap<>();
        for (final Map.Entry<String, List<Object>> entry : attributes.entrySet()) {
            final List<Object> values = entry.getValue();
            convertedAttributes.put(entry.getKey(), values.size() == 1 ? values.get(0) : values);
        }
        return convertedAttributes;
    }

    /***
     * Convert principal attributes to person attributes.
     * @param p  the principal carrying attributes
     * @return person attributes
     */
    private Map<String, List<Object>> convertPrincipalAttributesToPersonAttributes(final Principal p) {
        final Map<String, List<Object>> convertedAttributes = new HashMap<>(p.getAttributes().size());
        final Map<String, Object> principalAttributes = p.getAttributes();

        for (final Map.Entry<String, Object> entry : principalAttributes.entrySet()) {
            final Object values = entry.getValue();
            final String key = entry.getKey();
            if (values instanceof List) {
                convertedAttributes.put(key, (List) values);
            } else {
                convertedAttributes.put(key, Collections.singletonList(values));
            }
        }
        return convertedAttributes;
    }

    /**
     * Obtains attributes first from the repository by calling
     * {@link org.jasig.services.persondir.IPersonAttributeDao#getPerson(String)}.
     *
     * @param id the person id to locate in the attribute repository
     * @return the map of attributes
     */
    protected final Map<String, List<Object>> retrievePersonAttributesToPrincipalAttributes(final String id) {

        final IPersonAttributes attrs = getAttributeRepository().getPerson(id);

        if (attrs == null) {
            logger.debug("Could not find principal [{}] in the repository so no attributes are returned.", id);
            return Collections.emptyMap();
        }

        final Map<String, List<Object>> attributes = attrs.getAttributes();
        if (attributes == null) {
            logger.debug("Principal [{}] has no attributes and so none are returned.", id);
            return Collections.emptyMap();
        }
        return attributes;
    }

    @Override
    public final Map<String, Object> getAttributes(final Principal p) {
        final Map<String, Object> cachedAttributes = getPrincipalAttributes(p);
        if (cachedAttributes != null && !cachedAttributes.isEmpty()) {
            logger.debug("Found [{}] cached attributes for principal [{}]", cachedAttributes.size(), p.getId());
            return cachedAttributes;
        }

        if (getAttributeRepository() == null) {
            logger.debug("No attribute repository is defined for [{}]. Returning default principal attributes for {}",
                    getClass().getName(), p.getId());
            return cachedAttributes;
        }

        final Map<String, List<Object>> sourceAttributes = retrievePersonAttributesToPrincipalAttributes(p.getId());
        logger.debug("Found [{}] attributes for principal [{}] from the attribute repository.",
                sourceAttributes.size(), p.getId());

        if (this.mergingStrategy == null || this.mergingStrategy.getAttributeMerger() == null) {
            logger.debug("No merging strategy found, so attributes retrieved from the repository will be used instead.");
            return convertAttributesToPrincipalAttributesAndCache(p, sourceAttributes);
        }

        final Map<String, List<Object>> principalAttributes = convertPrincipalAttributesToPersonAttributes(p);

        logger.debug("Merging current principal attributes with that of the repository via strategy [{}]",
                this.mergingStrategy.getClass().getSimpleName());
        final Map<String, List<Object>> mergedAttributes =
                this.mergingStrategy.getAttributeMerger().mergeAttributes(principalAttributes, sourceAttributes);

        return convertAttributesToPrincipalAttributesAndCache(p, mergedAttributes);
    }

    /**
     * Convert attributes to principal attributes and cache.
     *
     * @param p the p
     * @param sourceAttributes the source attributes
     * @return the map
     */
    private Map<String, Object> convertAttributesToPrincipalAttributesAndCache(final Principal p,
                                                        final Map<String, List<Object>>  sourceAttributes) {
        final Map<String, Object> finalAttributes = convertPersonAttributesToPrincipalAttributes(sourceAttributes);
        addPrincipalAttributes(p.getId(), finalAttributes);
        return finalAttributes;
    }

    /**
     * Add principal attributes into the underlying cache instance.
     * @param id identifier used by the cache as key.
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
        if (this.attributeRepository == null) {
            final ApplicationContext context = ApplicationContextProvider.getApplicationContext();
            if (context != null) {
                return context.getBean("attributeRepository", IPersonAttributeDao.class);
            } else {
                logger.warn("No application context could be retrieved, so no attribute repository instance can be determined.");
            }
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

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
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
