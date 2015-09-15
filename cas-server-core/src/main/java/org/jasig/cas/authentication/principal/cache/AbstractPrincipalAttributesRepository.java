/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.authentication.principal.cache;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalAttributesRepository;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
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
    protected static final TimeUnit DEFAULT_CACHE_EXPIRATION_UNIT = TimeUnit.HOURS;

    /** Default expiration lifetime based on the default time unit. */
    protected static final long DEFAULT_CACHE_EXPIRATION_DURATION = 2;

    private static final long serialVersionUID = 6350245643948535906L;

    /** Logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final IPersonAttributeDao attributeRepository;

    /**
     * The merging strategy that deals with existing principal attributes
     * and those that are retrieved from the source. By default, existing attributes
     * are ignored and the source is always consulted.
     */
    private IAttributeMerger mergingStrategy;

    /**
     * Instantiates a new principal attributes repository.
     * Simply used buy
     */
    protected AbstractPrincipalAttributesRepository() {
        this.attributeRepository = null;
    }

    /**
     * Instantiates a new principal attributes repository.
     *
     * @param attributeRepository the attribute repository
     */
    public AbstractPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    protected final IPersonAttributeDao getAttributeRepository() {
        return this.attributeRepository;
    }

    /**
     * The merging strategy that deals with existing principal attributes
     * and those that are retrieved from the source. By default, existing attributes
     * are ignored and the source is always consulted.
     * @param mergingStrategy the strategy to use for conflicts
     */
    public final void setMergingStrategy(final IAttributeMerger mergingStrategy) {
        this.mergingStrategy = mergingStrategy;
    }

    public final IAttributeMerger getMergingStrategy() {
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

        final IPersonAttributes personAttributes = this.attributeRepository.getPerson(id);
        if (personAttributes == null) {
            logger.debug("Could not find principal [{}] in the repository so no attributes are returned.", id);
            return Collections.emptyMap();
        }

        final Map<String, List<Object>> attributes = personAttributes.getAttributes();
        if (attributes == null) {
            logger.debug("Principal [{}] has no attributes and so none are returned.", id);
            return Collections.emptyMap();
        }
        return attributes;
    }

    @Override
    public final Map<String, Object> getAttributes(@NotNull final Principal p) {
        final Map<String, Object> cachedAttributes = getPrincipalAttributesFromCache(p);
        if (cachedAttributes != null && !cachedAttributes.isEmpty()) {
            logger.debug("Found [{}] cached attributes for principal [{}]", cachedAttributes.size(), p.getId());
            return cachedAttributes;
        }

        final Map<String, List<Object>> sourceAttributes = retrievePersonAttributesToPrincipalAttributes(p.getId());
        logger.debug("Found [{}] attributes for principal [{}] from the attribute repository.",
                sourceAttributes.size(), p.getId());

        if (this.mergingStrategy == null) {
            logger.debug("No merging strategy found, so attributes retrieved from the repository will be used instead.");
            return convertAttributesToPrincipalAttributesAndCache(p, sourceAttributes);
        }

        final Map<String, List<Object>> principalAttributes = convertPrincipalAttributesToPersonAttributes(p);

        logger.debug("Merging current principal attributes with that of the repository via strategy [{}]",
                this.mergingStrategy.getClass().getSimpleName());
        final Map<String, List<Object>> mergedAttributes =
                this.mergingStrategy.mergeAttributes(principalAttributes, sourceAttributes);

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
        addPrincipalAttributesIntoCache(p.getId(), finalAttributes);
        return finalAttributes;
    }

    /**
     * Add principal attributes into the underlying cache instance.
     * @param id identifier used by the cache as key.
     * @param attributes attributes to cache
     * @since 4.2
     */
    protected abstract void addPrincipalAttributesIntoCache(String id, Map<String, Object> attributes);

    /**
     * Gets principal attributes from cache.
     *
     * @param p the principal
     * @return the principal attributes from cache
     */
    protected abstract Map<String, Object> getPrincipalAttributesFromCache(Principal p);

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("attributeRepository", attributeRepository)
                .append("mergingStrategy", mergingStrategy)
                .toString();
    }
}
