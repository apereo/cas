package org.apereo.cas.services;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link AbstractMultifactorAuthenticationProvider} is responsible for
 * as the parent of all providers.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
public abstract class AbstractMultifactorAuthenticationProvider implements MultifactorAuthenticationProvider, Serializable {

    private static final long serialVersionUID = 4789727148134156909L;

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * CAS Properties.
     */
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Override
    public final boolean supports(final Event e,
                                  final Authentication authentication,
                                  final RegisteredService registeredService) {
        if (e == null || !e.getId().equals(getId())) {
            logger.debug("Provided event id {} is not applicable to this provider identified by {}", getId());
            return false;
        }
        final MultifactorAuthenticationProperties.BaseProvider.Bypass bypass = getMultifactorProviderBypassProperties();
        final Principal principal = authentication.getPrincipal();
        final boolean supportsByPrincipal = skipBypassAndSupportEventBasedOnPrincipalAttributes(bypass, principal);
        final boolean supportsByAuthn = skipBypassAndSupportEventBasedOnAuthenticationAttributes(bypass, authentication);

        if (!supportsByPrincipal) {
            logger.debug("Bypass rules for principal {} indicate the requeste may be ignored by {}", principal.getId(), getId());
            return false;
        }
        if (!supportsByAuthn) {
            logger.debug("Bypass rules for authentication {} indicate the request may be ignored by {}", principal.getId(), getId());
            return false;
        }

        return supportsInternal(e, authentication, registeredService);
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
    protected boolean supportsInternal(final Event e,
                                       final Authentication authentication,
                                       final RegisteredService registeredService) {
        return true;
    }

    @Override
    public boolean isAvailable(final RegisteredService service) throws AuthenticationException {
        RegisteredServiceMultifactorPolicy.FailureModes failureMode = RegisteredServiceMultifactorPolicy.FailureModes.CLOSED;
        final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
        if (policy != null) {
            failureMode = policy.getFailureMode();
            logger.debug("Multifactor failure mode for {} is defined as {}", service.getServiceId(), failureMode);
        } else if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGlobalFailureMode())) {
            failureMode = RegisteredServiceMultifactorPolicy.FailureModes.valueOf(casProperties.getAuthn().getMfa().getGlobalFailureMode());
            logger.debug("Using global multifactor failure mode for {} defined as {}", service.getServiceId(), failureMode);
        }

        if (failureMode != RegisteredServiceMultifactorPolicy.FailureModes.NONE) {
            if (isAvailable()) {
                return true;
            }
            if (failureMode == RegisteredServiceMultifactorPolicy.FailureModes.CLOSED) {
                logger.warn("{} could not be reached. Authentication shall fail for {}",
                        getClass().getSimpleName(), service.getServiceId());
                throw new AuthenticationException();
            }

            logger.warn("{} could not be reached. Since the authentication provider is configured for the "
                            + "failure mode of {} authentication will proceed without {} for service {}",
                    getClass().getSimpleName(), failureMode, getClass().getSimpleName(), service.getServiceId());
            return false;
        }
        logger.debug("Failure mode is set to {}. Assuming the provider is available.", failureMode);
        return true;
    }

    /**
     * Skip bypass and support event based on authentication attributes.
     *
     * @param bypass the bypass
     * @param authn  the authn
     * @return the boolean
     */
    protected boolean skipBypassAndSupportEventBasedOnAuthenticationAttributes(
            final MultifactorAuthenticationProperties.BaseProvider.Bypass bypass, final Authentication authn) {
        return evaluateAttributeRulesForByPass(bypass.getAuthenticationAttributeName(),
                bypass.getAuthenticationAttributeValue(), authn.getAttributes());
    }

    /**
     * Skip bypass and support event based on principal attributes.
     *
     * @param bypass    the bypass
     * @param principal the principal
     * @return the boolean
     */
    protected boolean skipBypassAndSupportEventBasedOnPrincipalAttributes(
            final MultifactorAuthenticationProperties.BaseProvider.Bypass bypass, final Principal principal) {
        return evaluateAttributeRulesForByPass(bypass.getPrincipalAttributeName(),
                bypass.getAuthenticationAttributeValue(), principal.getAttributes());
    }

    /**
     * Evaluate attribute rules for bypass.
     *
     * @param attrName   the attr name
     * @param attrValue  the attr value
     * @param attributes the attributes
     * @return true if event should not be bypassed.
     */
    protected boolean evaluateAttributeRulesForByPass(final String attrName, final String attrValue,
                                                      final Map<String, Object> attributes) {
        boolean supports = true;
        if (StringUtils.isNotBlank(attrName)) {
            final Set<Map.Entry<String, Object>> names = attributes.entrySet().stream().filter(e ->
                    e.getKey().matches(attrName)
            ).collect(Collectors.toSet());

            supports = names.isEmpty();
            if (!names.isEmpty() && (StringUtils.isNotBlank(attrValue))) {
                final Set<Map.Entry<String, Object>> values = names.stream().filter(e -> {
                    final Set<Object> valuesCol = CollectionUtils.convertValueToCollection(e.getValue());
                    return valuesCol.stream()
                            .filter(v -> v.toString().matches(attrValue))
                            .findAny()
                            .isPresent();
                }).collect(Collectors.toSet());
                supports = values.isEmpty();

            }
        }
        return supports;
    }

    /**
     * Is provider available?
     *
     * @return the true/false
     */
    protected abstract boolean isAvailable();

    /**
     * Gets multifactor provider bypass properties.
     *
     * @return the multifactor provider bypass properties
     */
    protected abstract MultifactorAuthenticationProperties.BaseProvider.Bypass getMultifactorProviderBypassProperties();
    
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
}
