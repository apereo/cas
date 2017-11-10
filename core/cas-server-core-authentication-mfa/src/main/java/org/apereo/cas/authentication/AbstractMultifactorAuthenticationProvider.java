package org.apereo.cas.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

import static org.apereo.cas.services.RegisteredServiceMultifactorPolicy.FailureModes.*;

/**
 * The {@link AbstractMultifactorAuthenticationProvider} is responsible for
 * as the parent of all providers.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
public abstract class AbstractMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider, Serializable {

    private static final long serialVersionUID = 4789727148134156909L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMultifactorAuthenticationProvider.class);

    private MultifactorAuthenticationProviderBypass bypassEvaluator;

    private String globalFailureMode;

    private String id;

    private int order;

    public AbstractMultifactorAuthenticationProvider() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    public void setGlobalFailureMode(final String globalFailureMode) {
        this.globalFailureMode = globalFailureMode;
    }

    @Override
    public final boolean supports(final Event event, final Authentication authentication,
                                  final RegisteredService registeredService, final HttpServletRequest request) {
        if (event == null || !event.getId().matches(getId())) {
            LOGGER.debug("Provided event id [{}] is not applicable to this provider identified by [{}]", event, getId());
            return false;
        }

        if (bypassEvaluator != null
                && !bypassEvaluator.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, this, request)) {
            LOGGER.debug("Request cannot be supported by provider [{}] as it's configured for bypass", getId());
            return false;
        }

        if (supportsInternal(event, authentication, registeredService)) {
            LOGGER.debug("[{}] voted to support this authentication request", getClass().getSimpleName());
            return true;
        }

        LOGGER.debug("[{}] voted does not support this authentication request", getClass().getSimpleName());
        return false;
    }

    /**
     * Determine internally if provider is able to support this authentication request
     * for multifactor, and account for bypass rules..
     *
     * @param e                 the event
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @return the boolean
     */
    protected boolean supportsInternal(final Event e, final Authentication authentication, final RegisteredService registeredService) {
        return true;
    }

    @Override
    public boolean isAvailable(final RegisteredService service) throws AuthenticationException {
        RegisteredServiceMultifactorPolicy.FailureModes failureMode = CLOSED;
        
        if (StringUtils.isNotBlank(this.globalFailureMode)) {
            failureMode = RegisteredServiceMultifactorPolicy.FailureModes.valueOf(this.globalFailureMode);
            LOGGER.debug("Using global multi-factor failure mode for [{}] defined as [{}]", service, failureMode);
        }
        
        if (service != null) {
            LOGGER.debug("Evaluating multifactor authentication policy for service [{}}", service);
            final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
            if (policy.getFailureMode() != NOT_SET) {
                failureMode = policy.getFailureMode();
                LOGGER.debug("Multi-factor failure mode for [{}] is defined as [{}]", service.getServiceId(), failureMode);
            }
        }

        if (failureMode != RegisteredServiceMultifactorPolicy.FailureModes.NONE) {
            if (isAvailable()) {
                return true;
            }
            final String providerName = getClass().getSimpleName();
            if (failureMode == RegisteredServiceMultifactorPolicy.FailureModes.CLOSED) {
                LOGGER.warn("[{}] could not be reached. Authentication shall fail for [{}]", providerName, service);
                throw new AuthenticationException();
            }

            LOGGER.warn("[{}] could not be reached. Since the authentication provider is configured for the "
                            + "failure mode of [{}] authentication will proceed without [{}] for service [{}]",
                    providerName, failureMode, providerName, service);
            return false;
        }
        LOGGER.debug("Failure mode is set to [{}]. Assuming the provider is available.", failureMode);
        return true;
    }

    /**
     * Is provider available?
     *
     * @return the true/false
     */
    protected boolean isAvailable() {
        return true;
    }

    public void setBypassEvaluator(final MultifactorAuthenticationProviderBypass bypassEvaluator) {
        this.bypassEvaluator = bypassEvaluator;
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
        final MultifactorAuthenticationProvider rhs = (MultifactorAuthenticationProvider) obj;
        return new EqualsBuilder()
                .append(this.getOrder(), rhs.getOrder())
                .append(this.getId(), rhs.getId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getOrder())
                .append(getId())
                .toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean matches(final String identifier) {
        return getId().matches(identifier);
    }
}
