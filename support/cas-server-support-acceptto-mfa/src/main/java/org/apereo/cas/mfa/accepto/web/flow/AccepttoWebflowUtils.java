package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.Authentication;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.pac4j.core.context.J2EContext;

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
}
