package org.apereo.cas.ticket;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.Pac4jUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.profile.CommonProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link BaseIdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public abstract class BaseIdTokenGeneratorService implements IdTokenGeneratorService {
    /**
     * The cas properties.
     */
    protected final CasConfigurationProperties casProperties;
    /**
     * The Signing service.
     */
    protected final IdTokenSigningAndEncryptionService signingService;
    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Ticket registry.
     */
    protected final TicketRegistry ticketRegistry;
    /**
     * Gets authenticated profile.
     *
     * @param request  the request
     * @param response the response
     * @return the authenticated profile
     */
    protected CommonProfile getAuthenticatedProfile(final HttpServletRequest request, final HttpServletResponse response) {
        val manager = Pac4jUtils.getPac4jProfileManager(request, response);
        val profile = (Optional<CommonProfile>) manager.get(true);

        if (!profile.isPresent()) {
            throw new IllegalArgumentException("Unable to determine the user profile from the context");
        }
        return profile.get();
    }
}
