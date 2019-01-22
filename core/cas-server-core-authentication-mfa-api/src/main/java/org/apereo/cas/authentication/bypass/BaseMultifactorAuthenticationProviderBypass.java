package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link BaseMultifactorAuthenticationProviderBypass}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public abstract class BaseMultifactorAuthenticationProviderBypass implements MultifactorAuthenticationProviderBypass {
    /**
     * Method will remove any previous bypass set in the authentication.
     *
     * @param authentication - the authentication
     */
    @Override
    public void forgetBypass(final Authentication authentication) {
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.FALSE);
    }

    /**
     * Method will set the bypass into the authentication.
     *
     * @param authentication - the authentication
     * @param provider       - the provider
     */
    @Override
    public void rememberBypass(final Authentication authentication,
                               final MultifactorAuthenticationProvider provider) {
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.TRUE);
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, provider.getId());
    }

    /**
     * Locate matching attribute value boolean.
     *
     * @param attrName   the attr name
     * @param attrValue  the attr value
     * @param attributes the attributes
     * @return true/false
     */
    protected static boolean locateMatchingAttributeValue(final String attrName, final String attrValue,
                                                          final Map<String, Object> attributes) {
        return locateMatchingAttributeValue(attrName, attrValue, attributes, true);
    }

    /**
     * Evaluate attribute rules for bypass.
     *
     * @param attrName               the attr name
     * @param attrValue              the attr value
     * @param attributes             the attributes
     * @param matchIfNoValueProvided the force match on value
     * @return true a matching attribute name/value is found
     */
    protected static boolean locateMatchingAttributeValue(final String attrName, final String attrValue,
                                                          final Map<String, Object> attributes,
                                                          final boolean matchIfNoValueProvided) {
        LOGGER.debug("Locating matching attribute [{}] with value [{}] amongst the attribute collection [{}]", attrName, attrValue, attributes);
        if (StringUtils.isBlank(attrName)) {
            LOGGER.debug("Failed to match since attribute name is undefined");
            return false;
        }

        val names = attributes.entrySet()
            .stream()
            .filter(e -> {
                LOGGER.debug("Attempting to match [{}] against [{}]", attrName, e.getKey());
                return e.getKey().matches(attrName);
            })
            .collect(Collectors.toSet());

        LOGGER.debug("Found [{}] attributes relevant for multifactor authentication bypass", names.size());

        if (names.isEmpty()) {
            return false;
        }

        if (StringUtils.isBlank(attrValue)) {
            LOGGER.debug("No attribute value to match is provided; Match result is set to [{}]", matchIfNoValueProvided);
            return matchIfNoValueProvided;
        }

        val values = names
            .stream()
            .filter(e -> {
                val valuesCol = CollectionUtils.toCollection(e.getValue());
                LOGGER.debug("Matching attribute [{}] with values [{}] against [{}]", e.getKey(), valuesCol, attrValue);
                return valuesCol
                    .stream()
                    .anyMatch(v -> v.toString().matches(attrValue));
            }).collect(Collectors.toSet());

        LOGGER.debug("Matching attribute values remaining are [{}]", values);
        return !values.isEmpty();
    }
}
