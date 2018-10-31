package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicketFactory;
import org.apereo.cas.uma.ticket.resource.InvalidResourceSetException;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.repository.ResourceSetRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.Pac4jUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link BaseUmaEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public abstract class BaseUmaEndpointController {
    /**
     * Json object mapper instance.
     */
    protected static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    /**
     * The Uma permission ticket factory.
     */
    protected final UmaPermissionTicketFactory umaPermissionTicketFactory;

    /**
     * The Uma resource set repository.
     */
    protected final ResourceSetRepository umaResourceSetRepository;

    /**
     * The Cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * Gets authenticated profile.
     *
     * @param request            the request
     * @param response           the response
     * @param requiredPermission the required permission
     * @return the authenticated profile
     */
    protected CommonProfile getAuthenticatedProfile(final HttpServletRequest request,
                                                    final HttpServletResponse response,
                                                    final String requiredPermission) {
        val manager = Pac4jUtils.getPac4jProfileManager(request, response);
        val profile = (Optional<CommonProfile>) manager.get(true);
        if (profile == null || profile.isEmpty()) {
            throw new AuthenticationException("Unable to locate authenticated profile");
        }
        val p = profile.get();
        if (!p.getPermissions().contains(requiredPermission)) {
            throw new AuthenticationException("Authenticated profile does not carry the UMA protection scope");
        }
        return p;
    }

    /**
     * Build response entity error model.
     *
     * @param e the e
     * @return the multi value map
     */
    protected MultiValueMap<String, Object> buildResponseEntityErrorModel(final InvalidResourceSetException e) {
        return buildResponseEntityErrorModel(e.getStatus(), e.getMessage());
    }

    /**
     * Build response entity error model.
     *
     * @param code    the code
     * @param message the message
     * @return the multi value map
     */
    protected MultiValueMap<String, Object> buildResponseEntityErrorModel(final HttpStatus code, final String message) {
        return CollectionUtils.asMultiValueMap("code",
            code.value(),
            "message", message);
    }


    /**
     * Gets resource set uri location.
     *
     * @param saved the saved
     * @return the resource set uri location
     */
    protected String getResourceSetUriLocation(final ResourceSet saved) {
        return casProperties.getAuthn().getUma().getIssuer()
            + OAuth20Constants.BASE_OAUTH20_URL + "/"
            + OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/"
            + saved.getId();
    }
}
