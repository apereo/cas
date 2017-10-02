package org.apereo.cas.mgmt.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link CasUserProfileFactory}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 5.2.0
 */
public class CasUserProfileFactory {
    private final CasConfigurationProperties casProperties;

    public CasUserProfileFactory(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    /**
     * create user profile for views.
     *
     * @param request  the request
     * @param response the response
     * @return the cas user profile
     */
    public CasUserProfile from(final HttpServletRequest request, final HttpServletResponse response) {
        final ProfileManager manager = new ProfileManager(new J2EContext(request, response));
        final Optional<UserProfile> profile = manager.get(true);
        if (profile.isPresent()) {
            final UserProfile up = profile.get();
            return new CasUserProfile(up, this.casProperties.getMgmt().getAdminRoles());
        }
        throw new IllegalArgumentException("Could not determine authenticated profile");
    }
}
