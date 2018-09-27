package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.J2ESessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link Pac4jUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class Pac4jUtils {
    /**
     * Return the username of the authenticated user (based on pac4j security).
     *
     * @return the authenticated username.
     */
    public static String getPac4jAuthenticatedUsername() {
        final HttpServletRequest request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        final HttpServletResponse response = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
        if (request != null && response != null) {
            final ProfileManager manager = getPac4jProfileManager(request, response);
            final Optional<UserProfile> profile = manager.get(true);
            if (profile != null && profile.isPresent()) {
                final String id = profile.get().getId();
                if (id != null) {
                    return id;
                }
            }
        }
        return PrincipalResolver.UNKNOWN_USER;
    }

    /**
     * Gets pac 4 j profile manager.
     *
     * @param request  the request
     * @param response the response
     * @return the pac 4 j profile manager
     */
    public static ProfileManager getPac4jProfileManager(final HttpServletRequest request,
                                                        final HttpServletResponse response) {
        final J2EContext context = getPac4jJ2EContext(request, response, new J2ESessionStore());
        return getPac4jProfileManager(context);
    }

    /**
     * Gets pac 4 j profile manager.
     *
     * @param request      the request
     * @param response     the response
     * @param sessionStore the session store
     * @return the pac 4 j profile manager
     */
    public static ProfileManager getPac4jProfileManager(final HttpServletRequest request,
                                                        final HttpServletResponse response,
                                                        final SessionStore sessionStore) {
        final J2EContext context = getPac4jJ2EContext(request, response, sessionStore);
        return getPac4jProfileManager(context);
    }

    /**
     * Gets pac4j profile manager.
     *
     * @param context the context
     * @return the pac4j profile manager
     */
    public static ProfileManager getPac4jProfileManager(final WebContext context) {
        return new ProfileManager(context);
    }

    /**
     * Gets pac4j context.
     *
     * @param request      the request
     * @param response     the response
     * @param sessionStore the session store
     * @return the context
     */
    public static J2EContext getPac4jJ2EContext(final HttpServletRequest request,
                                                final HttpServletResponse response,
                                                final SessionStore sessionStore) {
        return new J2EContext(request, response, sessionStore);
    }

    /**
     * Gets pac4j context.
     *
     * @param request  the request
     * @param response the response
     * @return the pac4j context
     */
    public static J2EContext getPac4jJ2EContext(final HttpServletRequest request,
                                                final HttpServletResponse response) {
        return getPac4jJ2EContext(request, response, new J2ESessionStore());
    }

    /**
     * Gets pac4j context.
     *
     * @param request the request
     * @return the pac4j context
     */
    public static J2EContext getPac4jJ2EContext(final HttpServletRequest request) {
        return getPac4jJ2EContext(request, HttpRequestUtils.getHttpServletResponseFromRequestAttributes(), new J2ESessionStore());
    }

    /**
     * Gets pac4j context.
     *
     * @param request      the request
     * @param sessionStore the session store
     * @return the pac4j context
     */
    public static J2EContext getPac4jJ2EContext(final HttpServletRequest request, final SessionStore sessionStore) {
        return getPac4jJ2EContext(request,
            HttpRequestUtils.getHttpServletResponseFromRequestAttributes(),
            sessionStore);
    }

    /**
     * Gets pac4j context.
     *
     * @param sessionStore the session store
     * @return the pac4j context
     */
    public static J2EContext getPac4jJ2EContext(final SessionStore sessionStore) {
        return getPac4jJ2EContext(HttpRequestUtils.getHttpServletRequestFromRequestAttributes(),
            HttpRequestUtils.getHttpServletResponseFromRequestAttributes(),
            sessionStore);
    }

    /**
     * Gets pac4j context.
     *
     * @return the pac4j context
     */
    public static J2EContext getPac4jJ2EContext() {
        return getPac4jJ2EContext(HttpRequestUtils.getHttpServletRequestFromRequestAttributes(),
            HttpRequestUtils.getHttpServletResponseFromRequestAttributes(),
            new J2ESessionStore());
    }
}
