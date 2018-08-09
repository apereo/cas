package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.ticket.UmaPermissionTicketFactory;
import org.apereo.cas.uma.ticket.resource.repository.ResourceSetRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.Pac4jUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonObject;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link UmaPermissionRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Controller("umaPermissionRegistrationEndpointController")
@RequiredArgsConstructor
@Slf4j
public class UmaPermissionRegistrationEndpointController {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final UmaPermissionTicketFactory umaPermissionTicketFactory;
    private final ResourceSetRepository umaResourceSetRepository;

    /**
     * Gets permission ticket.
     *
     * @param body     the body
     * @param request  the request
     * @param response the response
     * @return the permission ticket
     */
    @PostMapping(value = '/' + OAuth20Constants.BASE_OAUTH20_URL + "/" + OAuth20Constants.UMA_PERMISSION_URL,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handle(@RequestBody final String body, final HttpServletRequest request, final HttpServletResponse response) {
        try {
            val profileResult = getAuthenticatedProfile(request, response);

            val umaRequest = MAPPER.readValue(body, UmaPermissionRegistrationRequest.class);
            val resourceSetResult = umaResourceSetRepository.getById(umaRequest.getId());

            if (!resourceSetResult.isPresent()) {
                val model = buildResponseEntityErrorModel(HttpStatus.NOT_FOUND, "Requested resource-set cannot be found");
                return new ResponseEntity(model, HttpStatus.BAD_REQUEST);
            }

            val resourceSet = resourceSetResult.get();
            if (!resourceSet.getOwner().equals(profileResult.getId())) {
                val model = buildResponseEntityErrorModel(HttpStatus.FORBIDDEN, "Resource-set owner does not match the authenticated profile");
                return new ResponseEntity(model, HttpStatus.BAD_REQUEST);
            }

            val permission = umaPermissionTicketFactory.create(resourceSet, umaRequest.getScopes());

            if (permission != null) {
                val entity = new JsonObject().add("ticket", permission.getId());
                val model = CollectionUtils.wrap("entity", entity, "code", HttpStatus.CREATED);
                return new ResponseEntity(model, HttpStatus.OK);
            }

            val model = buildResponseEntityErrorModel(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to generate permission ticket");
            return new ResponseEntity(model, HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity("Unable to complete the permission registration request.", HttpStatus.BAD_REQUEST);
    }

    private CommonProfile getAuthenticatedProfile(final HttpServletRequest request, final HttpServletResponse response) {
        val manager = Pac4jUtils.getPac4jProfileManager(request, response);
        val profile = (Optional<CommonProfile>) manager.get(true);
        if (profile == null || !profile.isPresent()) {
            throw new AuthenticationException("Unable to locate authenticated profile");
        }
        return profile.get();
    }

    private MultiValueMap<String, Object> buildResponseEntityErrorModel(final HttpStatus code, final String message) {
        return CollectionUtils.asMultiValueMap("code", code.value(), "message", message);
    }

    /**
     * The uma permission registration request.
     */
    @Data
    public static class UmaPermissionRegistrationRequest implements Serializable {
        private static final long serialVersionUID = 3614209506339611242L;

        @JsonProperty("resource_set_id")
        private long id;

        @JsonProperty
        private Collection<String> scopes;
    }
}
