package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link OidcAttributeToScopeClaimMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface OidcAttributeToScopeClaimMapper {
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(OidcAttributeToScopeClaimMapper.class);

    /**
     * The bean name of the default implementation.
     */
    String DEFAULT_BEAN_NAME = "oidcAttributeToScopeClaimMapper";

    /**
     * Gets mapped attribute.
     *
     * @param claim             the claim
     * @param registeredService the registered service
     * @return the mapped attribute
     */
    String getMappedAttribute(String claim, RegisteredService registeredService);

    /**
     * Contains mapped attribute boolean.
     *
     * @param claim             the claim
     * @param registeredService the registered service
     * @return true /false
     */
    boolean containsMappedAttribute(String claim, RegisteredService registeredService);

    /**
     * Map the claim to the mapped-name, or itself.
     *
     * @param claimName         the claim name
     * @param registeredService the registered service
     * @return the string
     */
    default String toMappedClaimName(final String claimName, final RegisteredService registeredService) {
        return containsMappedAttribute(claimName, registeredService)
            ? getMappedAttribute(claimName, registeredService)
            : claimName;
    }

    /**
     * Map claim and return values.
     *
     * @param claimName         the claim name
     * @param registeredService the registered service
     * @param principal         the principal
     * @param defaultValue      the default value
     * @return the list of values
     */
    default List<Object> mapClaim(final String claimName,
                                  final RegisteredService registeredService,
                                  final Principal principal,
                                  final Object defaultValue) {
        val attribute = toMappedClaimName(claimName, registeredService);
        val attributeValues = principal.getAttributes().containsKey(attribute)
            ? principal.getAttributes().get(attribute)
            : defaultValue;

        LOGGER.trace("Handling claim [{}] with value(s) [{}]", attribute, attributeValues);
        return CollectionUtils.toCollection(attributeValues)
            .stream()
            .map(value -> {
                if (value instanceof Boolean) {
                    return value;
                }
                val valueContent = value.toString();
                if (valueContent.equalsIgnoreCase(Boolean.FALSE.toString())
                    || valueContent.equalsIgnoreCase(Boolean.TRUE.toString())) {
                    return BooleanUtils.toBoolean(valueContent);
                }
                return value;
            })
            .collect(Collectors.toList());
    }
}
