package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.util.RegisteredServiceAccessStrategyEvaluator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import jakarta.persistence.PostLoad;
import java.io.Serial;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
@EqualsAndHashCode(callSuper = true)
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {

    @Serial
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

    protected RegisteredServiceAccessStrategyActivationCriteria activationCriteria;

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
        this.requiredAttributes = ObjectUtils.getIfNull(requiredAttributes, new HashMap<>());
        this.rejectedAttributes = ObjectUtils.getIfNull(rejectedAttributes, new HashMap<>());
    }

    public DefaultRegisteredServiceAccessStrategy(final Map<String, Set<String>> requiredAttributes) {
        this();
        this.requiredAttributes = ObjectUtils.getIfNull(requiredAttributes, new HashMap<>());
    }

    /**
     * Post load.
     */
    @PostLoad
    public void postLoad() {
        this.delegatedAuthenticationPolicy = ObjectUtils.getIfNull(this.delegatedAuthenticationPolicy,
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy());
        this.requiredAttributes = ObjectUtils.getIfNull(requiredAttributes, new HashMap<>());
        this.rejectedAttributes = ObjectUtils.getIfNull(rejectedAttributes, new HashMap<>());
    }

    @Override
    public boolean isServiceAccessAllowed(final RegisteredService registeredService, final Service service) {
        if (!this.enabled) {
            LOGGER.trace("Service is not enabled in service registry.");
            return false;
        }
        return true;
    }

    @Override
    public boolean isServiceAccessAllowedForSso(final RegisteredService registeredService) {
        if (!this.ssoEnabled) {
            LOGGER.trace("Service is not authorized to participate in SSO.");
            return false;
        }
        return true;
    }

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        val proceed = activationCriteria == null || activationCriteria.shouldActivate(request);
        if (proceed) {
            return RegisteredServiceAccessStrategyEvaluator
                .builder()
                .caseInsensitive(this.caseInsensitive)
                .requireAllAttributes(this.requireAllAttributes)
                .requiredAttributes(this.requiredAttributes)
                .rejectedAttributes(this.rejectedAttributes)
                .build()
                .apply(request);
        }
        return activationCriteria.isAllowIfInactive();
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
}
