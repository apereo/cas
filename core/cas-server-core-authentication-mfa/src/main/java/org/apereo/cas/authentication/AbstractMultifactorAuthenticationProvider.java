package org.apereo.cas.authentication;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;

import javax.servlet.http.HttpServletRequest;

import static org.apereo.cas.services.RegisteredServiceMultifactorPolicy.FailureModes.CLOSED;
import static org.apereo.cas.services.RegisteredServiceMultifactorPolicy.FailureModes.UNDEFINED;

/**
 * The {@link AbstractMultifactorAuthenticationProvider} is responsible for
 * as the parent of all providers.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"order", "id"})
public abstract class AbstractMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148134156909L;

    private MultifactorAuthenticationProviderBypass bypassEvaluator;

    private String globalFailureMode;

    private String id;

    private int order;

    @Override
    public final boolean supports(final Event event, final Authentication authentication, final RegisteredService registeredService, final HttpServletRequest request) {
        if (event == null || !event.getId().matches(getId())) {
            LOGGER.debug("Provided event id [{}] is not applicable to this provider identified by [{}]", event, getId());
            return false;
        }
        if (bypassEvaluator != null && !bypassEvaluator.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, this, request)) {
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
        var failureMode = CLOSED;
        if (StringUtils.isNotBlank(this.globalFailureMode)) {
            failureMode = RegisteredServiceMultifactorPolicy.FailureModes.valueOf(this.globalFailureMode);
            LOGGER.debug("Using global multi-factor failure mode for [{}] defined as [{}]", service, failureMode);
        }
        if (service != null) {
            LOGGER.debug("Evaluating multifactor authentication policy for service [{}}", service);
            val policy = service.getMultifactorPolicy();
            if (policy != null && policy.getFailureMode() != UNDEFINED) {
                failureMode = policy.getFailureMode();
                LOGGER.debug("Multi-factor failure mode for [{}] is defined as [{}]", service.getServiceId(), failureMode);
            }
        }
        if (failureMode != RegisteredServiceMultifactorPolicy.FailureModes.NONE) {
            if (isAvailable()) {
                return true;
            }
            val providerName = getClass().getSimpleName();
            if (failureMode == RegisteredServiceMultifactorPolicy.FailureModes.CLOSED) {
                LOGGER.warn("[{}] could not be reached. Authentication shall fail for [{}]", providerName, service);
                throw new AuthenticationException();
            }
            LOGGER.warn("[{}] could not be reached. Since the authentication provider is configured for the "
                + "failure mode of [{}] authentication will proceed without [{}] for service [{}]", providerName, failureMode, providerName, service.getServiceId());
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

    @Override
    public boolean matches(final String identifier) {
        return StringUtils.isNotBlank(getId()) ? getId().matches(identifier) : false;
    }
}
