package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.Authentication;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link AccepttoWebflowUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@UtilityClass
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
     * @param webContext   the web context
     * @param sessionStore the session store
     */
    public void resetChannelAndAuthentication(final JEEContext webContext, final SessionStore sessionStore) {
        sessionStore.set(webContext, SESSION_ATTRIBUTE_CHANNEL, null);
        sessionStore.set(webContext, SESSION_ATTRIBUTE_ORIGINAL_AUTHENTICATION, null);
    }

    /**
     * Gets channel.
     *
     * @param webContext   the web context
     * @param sessionStore the session store
     * @return the channel
     */
    public static Optional<Object> getChannel(final JEEContext webContext, final SessionStore sessionStore) {
        return sessionStore.get(webContext, SESSION_ATTRIBUTE_CHANNEL);
    }

    /**
     * Gets channel.
     *
     * @param requestContext the request context
     * @return the channel
     */
    public static Optional<String> getChannel(final RequestContext requestContext) {
        return Optional.ofNullable((String) requestContext.getFlowScope().get("accepttoChannel"));
    }

    /**
     * Gets authentication from session store.
     *
     * @param webContext   the web context
     * @param sessionStore the session store
     * @return the authentication from session store
     */
    public static Authentication getAuthentication(final JEEContext webContext, final SessionStore sessionStore) {
        val result = sessionStore.get(webContext, SESSION_ATTRIBUTE_ORIGINAL_AUTHENTICATION);
        return result.map(Authentication.class::cast).orElse(null);
    }

    /**
     * Store channel.
     *
     * @param channel      the channel
     * @param webContext   the web context
     * @param sessionStore the session store
     */
    public static void storeChannelInSessionStore(final String channel,
                                                  final JEEContext webContext, final SessionStore sessionStore) {
        sessionStore.set(webContext, SESSION_ATTRIBUTE_CHANNEL, channel);
    }

    /**
     * Store authentication.
     *
     * @param authentication the authentication
     * @param webContext     the web context
     * @param sessionStore   the session store
     */
    public static void storeAuthenticationInSessionStore(final Authentication authentication,
                                                         final JEEContext webContext, final SessionStore sessionStore) {
        sessionStore.set(webContext, SESSION_ATTRIBUTE_ORIGINAL_AUTHENTICATION, authentication);
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
     * Sets channel in webflow.
     *
     * @param requestContext the request context
     * @param channel        the channel
     */
    public void setChannel(final RequestContext requestContext, final String channel) {
        if (StringUtils.isBlank(channel)) {
            requestContext.getFlowScope().remove("accepttoChannel");
        } else {
            requestContext.getFlowScope().put("accepttoChannel", channel);
        }
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
