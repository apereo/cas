package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public class DefaultRegisteredServiceAccessStrategy implements RegisteredServiceAccessStrategy {

    private static final long serialVersionUID = 1245279151345635245L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegisteredServiceAccessStrategy.class);

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
     * Defines the attribute aggregation behavior when checking for required attributes.
     * Default requires that all attributes be present and match the principal's.
     */
    protected boolean requireAllAttributes = true;

    /**
     * Collection of required attributes
     * for this service to proceed.
     */
    protected Map<String, Set<String>> requiredAttributes = new HashMap<>();

    /**
     * Collection of attributes
     * that will be rejected which will cause this
     * policy to refuse access.
     */
    protected Map<String, Set<String>> rejectedAttributes = new HashMap<>();

    /**
     * Indicates whether matching on required attribute values
     * should be done in a case-insensitive manner.
     */
    protected boolean caseInsensitive;

    /**
     * Instantiates a new Default registered service authorization strategy.
     * By default, rules indicate that services are both enabled
     * and can participate in SSO.
     */
    public DefaultRegisteredServiceAccessStrategy() {
        this(true, true);
    }

    /**
     * Instantiates a new Default registered service authorization strategy.
     *
     * @param enabled    the enabled
     * @param ssoEnabled the sso enabled
     */
    public DefaultRegisteredServiceAccessStrategy(final boolean enabled, final boolean ssoEnabled) {
        this.enabled = enabled;
        this.ssoEnabled = ssoEnabled;
    }

    /**
     * Instantiates a new Default registered service access strategy.
     *
     * @param requiredAttributes the required attributes
     * @param rejectedAttributes the rejected attributes
     */
    public DefaultRegisteredServiceAccessStrategy(final Map<String, Set<String>> requiredAttributes,
                                                  final Map<String, Set<String>> rejectedAttributes) {
        this();
        this.requiredAttributes = requiredAttributes;
        this.rejectedAttributes = rejectedAttributes;
    }

    /**
     * Instantiates a new Default registered service access strategy.
     *
     * @param requiredAttributes the required attributes
     */
    public DefaultRegisteredServiceAccessStrategy(final Map<String, Set<String>> requiredAttributes) {
        this();
        this.requiredAttributes = requiredAttributes;
    }

    /**
     * Sets enabled.
     *
     * @param enabled the enabled
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Set to enable/authorize this service.
     *
     * @param ssoEnabled true to enable service
     */
    public void setSsoEnabled(final boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    /**
     * Is enabled boolean.
     *
     * @return the boolean
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Is sso enabled boolean.
     *
     * @return the boolean
     */
    public boolean isSsoEnabled() {
        return this.ssoEnabled;
    }

    /**
     * Defines the attribute aggregation when checking for required attributes.
     * Default requires that all attributes be present and match the principal's.
     *
     * @param requireAllAttributes the require all attributes
     */
    public void setRequireAllAttributes(final boolean requireAllAttributes) {
        this.requireAllAttributes = requireAllAttributes;
    }

    /**
     * Is require all attributes boolean.
     *
     * @return the boolean
     */
    public boolean isRequireAllAttributes() {
        return this.requireAllAttributes;
    }

    /**
     * Gets required attributes.
     *
     * @return the required attributes
     */
    public Map<String, Set<String>> getRequiredAttributes() {
        return new HashMap<>(this.requiredAttributes);
    }

    /**
     * Sets unauthorized redirect url.
     *
     * @param unauthorizedRedirectUrl the unauthorized redirect url
     */
    public void setUnauthorizedRedirectUrl(final URI unauthorizedRedirectUrl) {
        this.unauthorizedRedirectUrl = unauthorizedRedirectUrl;
    }

    @Override
    public URI getUnauthorizedRedirectUrl() {
        return this.unauthorizedRedirectUrl;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    /**
     * Is attribute value matching case insensitive?
     *
     * @return true /false
     */
    public boolean isCaseInsensitive() {
        return this.caseInsensitive;
    }

    /**
     * Sets case insensitive.
     *
     * @param caseInsensitive the case insensitive
     * @since 5.0.0
     */
    public void setCaseInsensitive(final boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Defines the required attribute names and values that
     * must be available to the principal before the flow
     * can proceed to the next step. Every attribute in
     * the map can be linked to multiple values.
     *
     * @param requiredAttributes the required attributes
     */
    public void setRequiredAttributes(final Map<String, Set<String>> requiredAttributes) {
        this.requiredAttributes = requiredAttributes;
    }

    /**
     * Sets rejected attributes. If the policy finds any of the attributes defined
     * here, it will simply reject and refuse access.
     *
     * @param rejectedAttributes the rejected attributes
     */
    public void setRejectedAttributes(final Map<String, Set<String>> rejectedAttributes) {
        this.rejectedAttributes = rejectedAttributes;
    }

    /**
     * Gets rejected attributes.
     *
     * @return the rejected attributes
     */
    public Map<String, Set<String>> getRejectedAttributes() {
        return this.rejectedAttributes;
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
        if (this.rejectedAttributes.isEmpty() && this.requiredAttributes.isEmpty()) {
            LOGGER.debug("Skipping access strategy policy, since no attributes rules are defined");
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
            LOGGER.debug("Access is denied. The principal does not have the required attributes specified by this strategy");
            return false;
        }


        return true;
    }

    /**
     * Do required attributes allow principal access boolean.
     *
     * @param principalAttributes the principal attributes
     * @param requiredAttributes  the required attributes
     * @return the boolean
     */
    protected boolean doRequiredAttributesAllowPrincipalAccess(final Map<String, Object> principalAttributes,
                                                             final Map<String, Set<String>> requiredAttributes) {
        LOGGER.debug("These required attributes [{}] are examined against [{}] before service can proceed.", requiredAttributes, principalAttributes);
        if (requiredAttributes.isEmpty()) {
            return true;
        }

        return common(principalAttributes, requiredAttributes);
    }

    /**
     * Do rejected attributes refuse principal access boolean.
     *
     * @param principalAttributes the principal attributes
     * @return the boolean
     */
    protected boolean doRejectedAttributesRefusePrincipalAccess(final Map<String, Object> principalAttributes) {
        LOGGER.debug("These rejected attributes [{}] are examined against [{}] before service can proceed.", rejectedAttributes, principalAttributes);
        if (rejectedAttributes.isEmpty()) {
            return false;
        }
        return common(principalAttributes, rejectedAttributes);
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
                            + "which means the principal is not carrying enough data to grant authorization",
                    principalAttributes);
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
                            + "which indicates the principal is not carrying enough data to grant authorization",
                    principalAttributes);
            return false;
        }
        return true;
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
        final DefaultRegisteredServiceAccessStrategy rhs = (DefaultRegisteredServiceAccessStrategy) obj;
        return new EqualsBuilder()
                .append(this.enabled, rhs.enabled)
                .append(this.ssoEnabled, rhs.ssoEnabled)
                .append(this.requireAllAttributes, rhs.requireAllAttributes)
                .append(this.requiredAttributes, rhs.requiredAttributes)
                .append(this.unauthorizedRedirectUrl, rhs.unauthorizedRedirectUrl)
                .append(this.caseInsensitive, rhs.caseInsensitive)
                .append(this.rejectedAttributes, rhs.rejectedAttributes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.enabled)
                .append(this.ssoEnabled)
                .append(this.requireAllAttributes)
                .append(this.requiredAttributes)
                .append(this.unauthorizedRedirectUrl)
                .append(this.caseInsensitive)
                .append(this.rejectedAttributes)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("enabled", this.enabled)
                .append("ssoEnabled", this.ssoEnabled)
                .append("requireAllAttributes", this.requireAllAttributes)
                .append("requiredAttributes", this.requiredAttributes)
                .append("unauthorizedRedirectUrl", this.unauthorizedRedirectUrl)
                .append("caseInsensitive", this.caseInsensitive)
                .append("rejectedAttributes", this.rejectedAttributes)
                .toString();
    }

    /**
     * Common boolean.
     *
     * @param principalAttributes the principal attributes
     * @param attributes          the attributes
     * @return the boolean
     */
    protected boolean common(final Map<String, Object> principalAttributes, final Map<String, Set<String>> attributes) {
        final Set<String> difference = attributes.keySet().stream()
                .filter(a -> principalAttributes.keySet().contains(a))
                .collect(Collectors.toSet());

        if (this.requireAllAttributes && difference.size() < attributes.size()) {
            return false;
        }

        return difference.stream().anyMatch(key -> {
            final Set<String> values = attributes.get(key);
            final Set<Object> availableValues = CollectionUtils.toCollection(principalAttributes.get(key));

            final Pattern pattern = RegexUtils.concatenate(values, this.caseInsensitive);
            if (pattern != RegexUtils.MATCH_NOTHING_PATTERN) {
                return availableValues.stream().map(Object::toString).anyMatch(pattern.asPredicate());
            }
            return availableValues.stream().anyMatch(values::contains);
        });
    }
}
