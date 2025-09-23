package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.ticket.resource.InvalidResourceSetException;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.AbstractController;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseUmaEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseUmaEndpointController extends AbstractController {
    /**
     * Json object mapper instance.
     */
    protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final UmaConfigurationContext umaConfigurationContext;

    protected UserProfile getAuthenticatedProfile(final HttpServletRequest request,
                                                  final HttpServletResponse response,
                                                  final String requiredPermission) {
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, getUmaConfigurationContext().getSessionStore());
        val profileResult = manager.getProfile();
        if (profileResult.isEmpty()) {
            throw new AuthenticationException("Unable to locate authenticated profile");
        }
        val profile = profileResult.get();
        if (!profile.getRoles().contains(requiredPermission)) {
            throw new AuthenticationException("Authenticated profile does not carry the UMA protection scope");
        }
        return profile;
    }

    protected MultiValueMap<String, Object> buildResponseEntityErrorModel(final InvalidResourceSetException e) {
        return buildResponseEntityErrorModel(e.getStatus(), e.getMessage());
    }

    protected MultiValueMap<String, Object> buildResponseEntityErrorModel(final HttpStatus code, final String message) {
        return CollectionUtils.asMultiValueMap("code",
            code.value(),
            "message", message);
    }

    protected OAuth20AccessToken resolveAccessToken(final Ticket token) {
        return (OAuth20AccessToken) (token.isStateless() ? umaConfigurationContext.getTicketRegistry().getTicket(token.getId()) : token);
    }

    protected String getResourceSetUriLocation(final ResourceSet saved) {
        return getUmaConfigurationContext().getCasProperties()
                   .getAuthn().getOauth().getUma().getCore().getIssuer()
               + OAuth20Constants.BASE_OAUTH20_URL + '/'
               + OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + '/'
               + saved.getId();
    }
}
