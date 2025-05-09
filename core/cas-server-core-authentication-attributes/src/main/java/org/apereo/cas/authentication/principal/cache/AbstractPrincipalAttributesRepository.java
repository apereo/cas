package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serial;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"mergingStrategy", "attributeRepositoryIds"})
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Accessors(chain = true)
@Getter
@Setter
public abstract class AbstractPrincipalAttributesRepository implements RegisteredServicePrincipalAttributesRepository {
    @Serial
    private static final long serialVersionUID = 6350245643948535906L;

    /**
     * The merging strategy that deals with existing principal attributes
     * and those that are retrieved from the source. By default, existing attributes
     * are ignored and the source is always consulted.
     */
    private PrincipalAttributesCoreProperties.MergingStrategyTypes mergingStrategy =
        PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED;

    private Set<String> attributeRepositoryIds = new LinkedHashSet<>();

    private boolean ignoreResolvedAttributes;

    protected static Map<String, List<Object>> convertPrincipalAttributesToPersonAttributes(final Map<String, ?> attributes) {
        val convertedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        val principalAttributes = new LinkedHashMap<>(attributes);
        LOGGER.trace("Principal attributes to convert to person attributes are [{}]", principalAttributes);
        principalAttributes.forEach((key, values) -> {
            if (values instanceof final Collection collection) {
                val uniqueValues = new LinkedHashSet<Object>(collection);
                val listedValues = new ArrayList<Object>(uniqueValues);
                convertedAttributes.put(key, listedValues);
            } else {
                convertedAttributes.put(key, CollectionUtils.wrap(values));
            }
        });
        LOGGER.trace("Converted principal attributes, now as person attributes are [{}]", convertedAttributes);
        return convertedAttributes;
    }

    protected static Map<String, List<Object>> convertPersonAttributesToPrincipalAttributes(final Map<String, List<Object>> attributes) {
        return attributes.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected Map<String, List<Object>> convertAttributesToPrincipalAttributesAndCache(final Map<String, List<Object>> sourceAttributes,
                                                                                       final RegisteredServiceAttributeReleasePolicyContext context) {
        val finalAttributes = convertPersonAttributesToPrincipalAttributes(sourceAttributes);
        update(context.getPrincipal().getId(), finalAttributes, context);
        LOGGER.trace("Final principal attributes after caching, if any, are [{}]", finalAttributes);
        return finalAttributes;
    }

    protected PrincipalAttributesCoreProperties.MergingStrategyTypes determineMergingStrategy() {
        return ObjectUtils.defaultIfNull(getMergingStrategy(), PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
    }

    @JsonIgnore
    protected boolean areAttributeRepositoryIdsDefined() {
        return attributeRepositoryIds != null && !attributeRepositoryIds.isEmpty();
    }

    protected Map<String, List<Object>> retrievePersonAttributesFromAttributeRepository(
        final RegisteredServiceAttributeReleasePolicyContext context) {
        val repository = context.getApplicationContext().getBean(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY, PersonAttributeDao.class);
        return PrincipalAttributeRepositoryFetcher
            .builder()
            .attributeRepository(repository)
            .principalId(context.getPrincipal().getId())
            .activeAttributeRepositoryIdentifiers(this.attributeRepositoryIds)
            .currentPrincipal(context.getPrincipal())
            .build()
            .retrieve();
    }

    @JsonIgnore
    protected Map<String, List<Object>> getPrincipalAttributes(final Principal principal) {
        if (ignoreResolvedAttributes) {
            return new HashMap<>();
        }
        return convertPrincipalAttributesToPersonAttributes(principal.getAttributes());
    }
}
