package org.apereo.cas.services;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultMultifactorAuthenticationProviderBypass}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultMultifactorAuthenticationProviderBypass implements MultifactorAuthenticationProviderBypass {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMultifactorAuthenticationProviderBypass.class);

    private MultifactorAuthenticationProperties.BaseProvider.Bypass bypass;

    public DefaultMultifactorAuthenticationProviderBypass(final MultifactorAuthenticationProperties.BaseProvider.Bypass bypass) {
        this.bypass = bypass;
    }
    
    @Override
    public boolean eval(final Authentication authentication) {

        final Principal principal = authentication.getPrincipal();
        final boolean supportsByPrincipal = skipBypassAndSupportEventBasedOnPrincipalAttributes(bypass, principal);
        if (!supportsByPrincipal) {
            LOGGER.debug("Bypass rules for principal {} indicate the request may be ignored", principal.getId());
            return false;
        }

        final boolean supportsByAuthn = skipBypassAndSupportEventBasedOnAuthenticationAttributes(bypass, authentication);
        if (!supportsByAuthn) {
            LOGGER.debug("Bypass rules for authentication {} indicate the request may be ignored", principal.getId());
            return false;
        }

        final boolean supportsByAuthnMethod = !evaluateAttributeRulesForBypass(
                AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE,
                bypass.getAuthenticationMethodName(),
                authentication.getAttributes()
        );
        if (!supportsByAuthnMethod) {
            LOGGER.debug("Bypass rules for authentication method {} indicate the request may be ignored", principal.getId());
            return false;
        }

        final boolean supportsByAuthnHandler = !evaluateAttributeRulesForBypass(
                AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS,
                bypass.getAuthenticationHandlerName(),
                authentication.getAttributes()
        );
        if (!supportsByAuthnHandler) {
            LOGGER.debug("Bypass rules for authentication handlers {} indicate the request may be ignored", principal.getId());
            return false;
        }

        final boolean supportsByCredentialType = !evaluateCredentialTypeForBypass(authentication, bypass.getCredentialClassType());
        if (!supportsByCredentialType) {
            LOGGER.debug("Bypass rules for credential types {} indicate the request may be ignored", principal.getId());
            return false;
        }

        return true;
    }

    private static boolean evaluateCredentialTypeForBypass(final Authentication authentication, final String credentialClassType) {
        return StringUtils.isNotBlank(credentialClassType) && authentication.getCredentials().stream()
                .filter(e -> e.getCredentialClass().getName().matches(credentialClassType))
                .findAny()
                .isPresent();
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
        return evaluateAttributeRulesForBypass(bypass.getAuthenticationAttributeName(),
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
        return evaluateAttributeRulesForBypass(bypass.getPrincipalAttributeName(),
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
    protected boolean evaluateAttributeRulesForBypass(final String attrName, final String attrValue,
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
}
