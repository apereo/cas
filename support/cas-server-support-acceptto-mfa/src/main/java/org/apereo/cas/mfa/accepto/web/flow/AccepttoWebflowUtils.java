package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.Authentication;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.J2EContext;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link AccepttoWebflowUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@UtilityClass
@Slf4j
public class AccepttoWebflowUtils {
    /**
     * Session attribute to hold the authentication channel.
     */
    private static final String SESSION_ATTRIBUTE_CHANNEL = "acceptoMfaChannel";

    /**
     * Session attribute to hold original authn.
     */
    private static final String SESSION_ATTRIBUTE_ORIGINAL_AUTHENTICATION = "acceptoMfaOriginalAuthN";

    /**
     * Reset acceptto session store.
     *
     * @param webContext the web context
     */
    public void resetChannelAndAuthentication(final J2EContext webContext) {
        val sessionStore = webContext.getSessionStore();
        sessionStore.set(webContext, SESSION_ATTRIBUTE_CHANNEL, null);
        sessionStore.set(webContext, SESSION_ATTRIBUTE_ORIGINAL_AUTHENTICATION, null);
    }

    /**
     * Gets channel.
     *
     * @param webContext the web context
     * @return the channel
     */
    public static Object getChannel(final J2EContext webContext) {
        return webContext.getSessionStore().get(webContext, SESSION_ATTRIBUTE_CHANNEL);
    }

    /**
     * Gets authentication from session store.
     *
     * @param webContext the web context
     * @return the authentication from session store
     */
    public static Authentication getAuthentication(final J2EContext webContext) {
        return (Authentication) webContext.getSessionStore().get(webContext, SESSION_ATTRIBUTE_ORIGINAL_AUTHENTICATION);
    }

    /**
     * Store channel.
     *
     * @param channel    the channel
     * @param webContext the web context
     */
    public static void storeChannel(final String channel, final J2EContext webContext) {
        webContext.getSessionStore().set(webContext, SESSION_ATTRIBUTE_CHANNEL, channel);
    }

    /**
     * Store authentication.
     *
     * @param authentication the authentication
     * @param webContext     the web context
     */
    public static void storeAuthentication(final Authentication authentication, final J2EContext webContext) {
        webContext.getSessionStore().set(webContext, SESSION_ATTRIBUTE_ORIGINAL_AUTHENTICATION, authentication);
    }

    /**
     * Sets invitation token.
     *
     * @param requestContext  the request context
     * @param invitationToken the invitation token
     */
    public static void setInvitationToken(final RequestContext requestContext, final String invitationToken) {
        requestContext.getFlowScope().put("accepttoInvitationToken", invitationToken);
    }

    /**
     * Sets eguardian user id.
     *
     * @param requestContext  the context
     * @param eguardianUserId the eguardian user id
     */
    public static void setEGuardianUserId(final RequestContext requestContext, final String eguardianUserId) {
        requestContext.getFlowScope().put("eguardianUserId", eguardianUserId);
    }

    /**
     * Gets eguardian user id.
     *
     * @param requestContext the request context
     * @return the e guardian user id
     */
    public static Optional<String> getEGuardianUserId(final RequestContext requestContext) {
        return Optional.ofNullable((String) requestContext.getFlowScope().get("eguardianUserId"));
    }

    /**
     * Sets application id.
     *
     * @param requestContext the request context
     * @param applicationId  the application id
     */
    public static void setApplicationId(final RequestContext requestContext, final String applicationId) {
        requestContext.getFlowScope().put("accepttoApplicationId", applicationId);
    }

    /**
     * Sets invitation token qr code.
     *
     * @param requestContext the request context
     * @param qrHash         the qr hash
     */
    public static void setInvitationTokenQRCode(final RequestContext requestContext, final String qrHash) {
        requestContext.getFlowScope().put("accepttoInvitationTokenQRCodeHash", qrHash);
    }
}
