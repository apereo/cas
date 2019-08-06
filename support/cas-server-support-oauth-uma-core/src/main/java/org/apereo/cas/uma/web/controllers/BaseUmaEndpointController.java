package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.ticket.resource.InvalidResourceSetException;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseUmaEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@RequiredArgsConstructor
public abstract class BaseUmaEndpointController {
    /**
     * Json object mapper instance.
     */
    protected static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules();

    private final UmaConfigurationContext umaConfigurationContext;

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
        val context = new JEEContext(request, response, getUmaConfigurationContext().getSessionStore());
        val manager = new ProfileManager<CommonProfile>(context, context.getSessionStore());
        val profileResult = manager.get(true);
        if (profileResult.isEmpty()) {
            throw new AuthenticationException("Unable to locate authenticated profile");
        }
        val profile = profileResult.get();
        if (!profile.getPermissions().contains(requiredPermission)) {
            throw new AuthenticationException("Authenticated profile does not carry the UMA protection scope");
        }
        return profile;
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
        return getUmaConfigurationContext().getCasProperties()
            .getAuthn().getUma().getIssuer()
            + OAuth20Constants.BASE_OAUTH20_URL + '/'
            + OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + '/'
            + saved.getId();
    }
}
