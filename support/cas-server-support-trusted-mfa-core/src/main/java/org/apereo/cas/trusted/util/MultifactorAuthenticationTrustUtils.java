package org.apereo.cas.trusted.util;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.trusted.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorAuthenticationTrustUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@UtilityClass
public class MultifactorAuthenticationTrustUtils {

    /**
     * Generate geography.
     *
     * @return the geography
     */
    public static String generateGeography() {
        val clientInfo = ClientInfoHolder.getClientInfo();
        return clientInfo.getClientIpAddress().concat("@").concat(WebUtils.getHttpServletRequestUserAgentFromRequestContext());
    }

    /**
     * Track trusted multifactor authentication attribute.
     *
     * @param authn         the authn
     * @param attributeName the attribute name
     */
    public static void trackTrustedMultifactorAuthenticationAttribute(
        final Authentication authn,
        final String attributeName) {

        val newAuthn = DefaultAuthenticationBuilder.newInstance(authn)
            .addAttribute(attributeName, Boolean.TRUE)
            .build();
        LOGGER.debug("Updated authentication session to remember trusted multifactor record via [{}]", attributeName);
        authn.update(newAuthn);
    }

    /**
     * Is multifactor authentication trusted in scope boolean.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean isMultifactorAuthenticationTrustedInScope(final RequestContext requestContext) {
        return requestContext.getFlashScope().contains(
            AbstractMultifactorTrustedDeviceWebflowConfigurer.MFA_TRUSTED_AUTHN_SCOPE_ATTR);
    }

    /**
     * Sets multifactor authentication trusted in scope.
     *
     * @param requestContext the request context
     */
    public static void setMultifactorAuthenticationTrustedInScope(final RequestContext requestContext) {
        val flashScope = requestContext.getFlashScope();
        flashScope.put(AbstractMultifactorTrustedDeviceWebflowConfigurer.MFA_TRUSTED_AUTHN_SCOPE_ATTR, Boolean.TRUE);
    }
}
