package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.PostLoad;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultRegisteredServiceAccessStrategy}
 * that allows the following rules:
 * <ul>
 * <li>A service may be disallowed to use CAS for authentication</li>
 * <li>A service may be disallowed to take part in CAS single sign-on such that
 * presentation of credentials would always be required.</li>
 * <li>A service may be prohibited from receiving a service ticket
 * if the existing principal attributes don't contain the required attributes
 * that otherwise grant access to the service.</li>
 * </ul>
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@ToString
@Getter
@EqualsAndHashCode
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultRegisteredServiceAccessStrategy implements RegisteredServiceAccessStrategy {

    private static final long serialVersionUID = 1245279151345635245L;

    /**
     * The sorting/execution order of this strategy.
     */
    protected int order;

    /**
     * Is the service allowed at all?
     */
    protected boolean enabled = true;

    /**
     * Is the service allowed to use SSO?
     */
    protected boolean ssoEnabled = true;

    /**
     * The Unauthorized redirect url.
     */
    protected URI unauthorizedRedirectUrl;

    /**
     * The delegated authn policy.
     */
    protected RegisteredServiceDelegatedAuthenticationPolicy delegatedAuthenticationPolicy =
        new DefaultRegisteredServiceDelegatedAuthenticationPolicy();

    /**
     * Defines the attribute aggregation behavior when checking for required attributes.
     * Default requires that all attributes be present and match the principal's.
     */
    protected boolean requireAllAttributes = true;

    /**
     * Collection of required attributes
     * for this service to proceed.
     */
    protected Map<String, Set<String>> requiredAttributes = new HashMap<>(0);

    /**
     * Collection of attributes
     * that will be rejected which will cause this
     * policy to refuse access.
     */
    protected Map<String, Set<String>> rejectedAttributes = new HashMap<>(0);

    /**
     * Indicates whether matching on required attribute values
     * should be done in a case-insensitive manner.
     */
    protected boolean caseInsensitive;

    public DefaultRegisteredServiceAccessStrategy() {
        this(true, true);
    }

    public DefaultRegisteredServiceAccessStrategy(final boolean enabled, final boolean ssoEnabled) {
        this.enabled = enabled;
        this.ssoEnabled = ssoEnabled;
    }

    public DefaultRegisteredServiceAccessStrategy(final Map<String, Set<String>> requiredAttributes,
                                                  final Map<String, Set<String>> rejectedAttributes) {
        this();
        this.requiredAttributes = ObjectUtils.defaultIfNull(requiredAttributes, new HashMap<>(0));
        this.rejectedAttributes = ObjectUtils.defaultIfNull(rejectedAttributes, new HashMap<>(0));
    }

    public DefaultRegisteredServiceAccessStrategy(final Map<String, Set<String>> requiredAttributes) {
        this();
        this.requiredAttributes = ObjectUtils.defaultIfNull(requiredAttributes, new HashMap<>(0));
    }

    /**
     * Post load.
     */
    @PostLoad
    public void postLoad() {
        this.delegatedAuthenticationPolicy = ObjectUtils.defaultIfNull(this.delegatedAuthenticationPolicy,
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy());
        this.requiredAttributes = ObjectUtils.defaultIfNull(requiredAttributes, new HashMap<>(0));
        this.rejectedAttributes = ObjectUtils.defaultIfNull(rejectedAttributes, new HashMap<>(0));
    }

    /**
     * Expose underlying attributes for auditing purposes.
     *
     * @return required attributes
     */
    @Override
    public Map<String, Set<String>> getRequiredAttributes() {
        return requiredAttributes;
    }

    @JsonIgnore
    @Override
    public boolean isServiceAccessAllowedForSso() {
        if (!this.ssoEnabled) {
            LOGGER.trace("Service is not authorized to participate in SSO.");
            return false;
        }
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isServiceAccessAllowed() {
        if (!this.enabled) {
            LOGGER.trace("Service is not enabled in service registry.");
            return false;
        }
        return true;
    }

    @JsonIgnore
    @Override
    public void setServiceAccessAllowed(final boolean value) {
        this.enabled = value;
    }

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> principalAttributes) {
        if ((this.rejectedAttributes == null || this.rejectedAttributes.isEmpty()) && (this.requiredAttributes == null || this.requiredAttributes.isEmpty())) {
            LOGGER.trace("Skipping access strategy policy, since no attributes rules are defined");
            return true;
        }
        if (!enoughAttributesAvailableToProcess(principal, principalAttributes)) {
            LOGGER.debug("Access is denied. There are not enough attributes available to satisfy requirements");
            return false;
        }
        if (doRejectedAttributesRefusePrincipalAccess(principalAttributes)) {
            LOGGER.debug("Access is denied. The principal carries attributes that would reject service access");
            return false;
        }
        if (!doRequiredAttributesAllowPrincipalAccess(principalAttributes, this.requiredAttributes)) {
            LOGGER.debug("Access is denied. The principal does not have the required attributes [{}] specified by this strategy", this.requiredAttributes);
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
        LOGGER.debug("These required attributes [{}] are examined against [{}] before service can proceed.", requiredAttributes, principalAttributes);
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
            .filter(a -> principalAttributes.keySet().contains(a))
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

    private boolean requiredAttributeFound(final String attributeName,
                                           final Map<String, Object> principalAttributes,
                                           final Map<String, Set<String>> requiredAttributes) {
        val values = requiredAttributes.get(attributeName);
        val availableValues = CollectionUtils.toCollection(principalAttributes.get(attributeName));
        val pattern = RegexUtils.concatenate(values, this.caseInsensitive);
        LOGGER.debug("Checking [{}] against [{}] with pattern [{}] for attribute [{}]", values, availableValues, pattern, attributeName);
        if (!pattern.equals(RegexUtils.MATCH_NOTHING_PATTERN)) {
            return availableValues.stream().map(Object::toString).anyMatch(pattern.asPredicate());
        }
        return availableValues.stream().anyMatch(values::contains);
    }
}
