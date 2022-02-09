package org.apereo.cas.services.util;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link RegisteredServiceAccessStrategyEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@Getter
@Slf4j
public class RegisteredServiceAccessStrategyEvaluator {
    /**
     * Collection of required attributes
     * for this service to proceed.
     */
    @Builder.Default
    private Map<String, Set<String>> requiredAttributes = new HashMap<>(0);

    /**
     * Collection of attributes
     * that will be rejected which will cause this
     * policy to refuse access.
     */
    @Builder.Default
    private Map<String, Set<String>> rejectedAttributes = new HashMap<>(0);

    /**
     * Indicates whether matching on required attribute values
     * should be done in a case-insensitive manner.
     */
    private boolean caseInsensitive;

    /**
     * Defines the attribute aggregation behavior when checking for required attributes.
     * Default requires that all attributes be present and match the principal's.
     */
    @Builder.Default
    private boolean requireAllAttributes = true;

    /**
     * Evaluate access.
     *
     * @param principal           the principal
     * @param attributes the principal attributes
     * @return the boolean
     */
    public boolean evaluate(final String principal, final Map<String, Object> attributes) {
        if ((this.rejectedAttributes == null || this.rejectedAttributes.isEmpty())
            && (this.requiredAttributes == null || this.requiredAttributes.isEmpty())) {
            LOGGER.trace("Skipping access strategy policy, since no attributes rules are defined");
            return true;
        }
        if (!enoughAttributesAvailableToProcess(principal, attributes)) {
            LOGGER.debug("Access is denied. There are not enough attributes available to satisfy requirements");
            return false;
        }
        if (doRejectedAttributesRefusePrincipalAccess(attributes)) {
            LOGGER.debug("Access is denied. The principal carries attributes that would reject service access");
            return false;
        }
        if (!doRequiredAttributesAllowPrincipalAccess(attributes, this.requiredAttributes)) {
            LOGGER.debug("Access is denied. The principal does not have the required attributes [{}]", this.requiredAttributes);
            return false;
        }
        return true;
    }

    /**
     * Do required attributes allow principal access boolean.
     *
     * @param principalAttributes the principal attributes
     * @param requiredAttributes  the required attributes
     * @return true/false
     */
    protected boolean doRequiredAttributesAllowPrincipalAccess(final Map<String, Object> principalAttributes,
                                                               final Map<String, Set<String>> requiredAttributes) {
        LOGGER.debug("These required attributes [{}] are examined against [{}] before service can proceed.",
            requiredAttributes, principalAttributes);
        return requiredAttributes.isEmpty() || requiredAttributesFoundInMap(principalAttributes, requiredAttributes);
    }

    /**
     * Do rejected attributes refuse principal access boolean.
     *
     * @param principalAttributes the principal attributes
     * @return true/false
     */
    protected boolean doRejectedAttributesRefusePrincipalAccess(final Map<String, Object> principalAttributes) {
        LOGGER.debug("These rejected attributes [{}] are examined against [{}] before service can proceed.", rejectedAttributes, principalAttributes);
        return !rejectedAttributes.isEmpty() && requiredAttributesFoundInMap(principalAttributes, rejectedAttributes);
    }

    /**
     * Enough attributes available to process? Check collection sizes and determine
     * if we have enough data to move on.
     *
     * @param principal           the principal
     * @param principalAttributes the principal attributes
     * @return true /false
     */
    protected boolean enoughAttributesAvailableToProcess(final String principal, final Map<String, Object> principalAttributes) {
        if (!enoughRequiredAttributesAvailableToProcess(principalAttributes, this.requiredAttributes)) {
            return false;
        }
        if (principalAttributes.size() < this.rejectedAttributes.size()) {
            LOGGER.debug("The size of the principal attributes that are [{}] does not match defined rejected attributes, "
                         + "which means the principal is not carrying enough data to grant authorization", principalAttributes);
            return false;
        }
        return true;
    }

    /**
     * Enough required attributes available to process? Check collection sizes and determine
     * if we have enough data to move on.
     *
     * @param principalAttributes the principal attributes
     * @param requiredAttributes  the required attributes
     * @return true /false
     */
    protected boolean enoughRequiredAttributesAvailableToProcess(final Map<String, Object> principalAttributes,
                                                                 final Map<String, Set<String>> requiredAttributes) {
        if (principalAttributes.isEmpty() && !requiredAttributes.isEmpty()) {
            LOGGER.debug("No principal attributes are found to satisfy defined attribute requirements");
            return false;
        }
        if (principalAttributes.size() < requiredAttributes.size()) {
            LOGGER.debug("The size of the principal attributes that are [{}] does not match defined required attributes, "
                         + "which indicates the principal is not carrying enough data to grant authorization", principalAttributes);
            return false;
        }
        return true;
    }

    /**
     * Check whether required attributes are found in the given map.
     *
     * @param principalAttributes the principal attributes
     * @param requiredAttributes  the attributes
     * @return true/false
     */
    protected boolean requiredAttributesFoundInMap(final Map<String, Object> principalAttributes,
                                                   final Map<String, Set<String>> requiredAttributes) {
        val difference = requiredAttributes.keySet()
            .stream()
            .filter(principalAttributes::containsKey)
            .collect(Collectors.toSet());
        LOGGER.debug("Difference of checking required attributes: [{}]", difference);
        if (this.requireAllAttributes && difference.size() < requiredAttributes.size()) {
            return false;
        }
        if (this.requireAllAttributes) {
            return difference.stream().allMatch(key -> requiredAttributeFound(key, principalAttributes, requiredAttributes));
        }
        return difference.stream().anyMatch(key -> requiredAttributeFound(key, principalAttributes, requiredAttributes));
    }

    /**
     * Required attribute found boolean.
     *
     * @param attributeName       the attribute name
     * @param principalAttributes the principal attributes
     * @param requiredAttributes  the required attributes
     * @return the boolean
     */
    protected boolean requiredAttributeFound(final String attributeName,
                                           final Map<String, Object> principalAttributes,
                                           final Map<String, Set<String>> requiredAttributes) {
        val requiredValues = requiredAttributes.get(attributeName);
        val availableValues = CollectionUtils.toCollection(principalAttributes.get(attributeName));
        val pattern = RegexUtils.concatenate(requiredValues, this.caseInsensitive);
        LOGGER.debug("Checking [{}] against [{}] with pattern [{}] for attribute [{}]",
            requiredValues, availableValues, pattern, attributeName);
        if (!pattern.equals(RegexUtils.MATCH_NOTHING_PATTERN)) {
            return availableValues.stream().map(Object::toString).anyMatch(pattern.asPredicate());
        }
        return availableValues.stream().anyMatch(requiredValues::contains);
    }
}
