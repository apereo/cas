package org.apereo.cas.support.pac4j.util;

import java.util.Optional;

import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;


/**
 * A collection of utility methods related to PAC4J.
 * 
 * @author jkacer
 * 
 * @since 5.1.6
 */
public final class Pac4JUtils {

    /**
     * Private constructor to disable instantiation.
     */
    private Pac4JUtils() {
        super();
    }


    /**
     * Finds the current client name from the context, using the PAC4J Profile Manager. It is assumed that the context has previously been
     * populated with the profile.
     * 
     * @param webContext
     *            A web context (request + response).
     * 
     * @return The currently used client's name or {@code null} if there is no active profile.
     */
    public static String findCurrentClientName(final WebContext webContext) {
        @SuppressWarnings("unchecked")
        final ProfileManager<? extends CommonProfile> pm = WebUtils.getPac4jProfileManager(webContext);
        final Optional<? extends CommonProfile> profile = pm.get(true);
        return profile.map(CommonProfile::getClientName).orElse(null);
    }

}
