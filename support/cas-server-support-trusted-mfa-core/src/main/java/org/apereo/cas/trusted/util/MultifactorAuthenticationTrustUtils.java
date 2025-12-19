package org.apereo.cas.trusted.util;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.trusted.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
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
     * @param authentication the authentication
     * @param attributeName  the attribute name
     */
    public static void trackTrustedMultifactorAuthenticationAttribute(final Authentication authentication, final String attributeName) {

        val newAuthn = DefaultAuthenticationBuilder.newInstance(authentication)
            .addAttribute(attributeName, Boolean.TRUE)
            .build();
        LOGGER.debug("Updated authentication session to remember trusted multifactor record via [{}]", attributeName);
        authentication.updateAttributes(newAuthn);
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

    /**
     * Put multifactor authentication trusted devices.
     *
     * @param requestContext the request context
     * @param accounts       the accounts
     */
    public static void putMultifactorAuthenticationTrustedDevices(final RequestContext requestContext, final List accounts) {
        requestContext.getFlowScope().put("multifactorTrustedDevices", accounts);
    }

    /**
     * Gets multifactor authentication trusted devices.
     *
     * @param requestContext the request context
     * @return the multifactor authentication trusted devices
     */
    public List getMultifactorAuthenticationTrustedDevices(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("multifactorTrustedDevices", List.class);
    }

    /**
     * Gets multifactor authentication trust record.
     *
     * @param <T>     the type parameter
     * @param context the context
     * @param clazz   the clazz
     * @return the multifactor authentication trust record
     */
    public static <T> Optional<T> getMultifactorAuthenticationTrustRecord(final RequestContext context, final Class<T> clazz) {
        return Optional.ofNullable(context.getFlowScope().get(CasWebflowConstants.VAR_ID_MFA_TRUST_RECORD, clazz));
    }

    /**
     * Put multifactor authentication trust record.
     *
     * @param context the context
     * @param object  the object
     */
    public static void putMultifactorAuthenticationTrustRecord(final RequestContext context, final Serializable object) {
        context.getFlowScope().put(CasWebflowConstants.VAR_ID_MFA_TRUST_RECORD, object);
    }

    /**
     * Put multifactor authentication trusted devices disabled.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putMultifactorAuthenticationTrustedDevicesDisabled(final RequestContext context, final boolean value) {
        context.getFlowScope().put("multifactorTrustedDevicesDisabled", value);
    }

    /**
     * Is multifactor authentication trusted devices disabled boolean.
     *
     * @param context the context
     * @return true/false
     */
    public static boolean isMultifactorAuthenticationTrustedDevicesDisabled(final RequestContext context) {
        return BooleanUtils.toBoolean(context.getFlowScope().getBoolean("multifactorTrustedDevicesDisabled", false));
    }
}
