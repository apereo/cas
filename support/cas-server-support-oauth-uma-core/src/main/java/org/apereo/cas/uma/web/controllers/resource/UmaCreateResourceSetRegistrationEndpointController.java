package org.apereo.cas.uma.web.controllers.resource;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.ticket.resource.InvalidResourceSetException;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointController;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link UmaCreateResourceSetRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Controller("umaCreateResourceSetRegistrationEndpointController")
@Slf4j
public class UmaCreateResourceSetRegistrationEndpointController extends BaseUmaEndpointController {

    public UmaCreateResourceSetRegistrationEndpointController(final UmaConfigurationContext umaConfigurationContext) {
        super(umaConfigurationContext);
    }

    /**
     * Register resource-set.
     *
     * @param body     the body
     * @param request  the request
     * @param response the response
     * @return the permission ticket
     */
    @PostMapping(value = '/' + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity registerResourceSet(@RequestBody final String body, final HttpServletRequest request, final HttpServletResponse response) {
        try {
            val profileResult = getAuthenticatedProfile(request, response, OAuth20Constants.UMA_PROTECTION_SCOPE);

            val umaRequest = MAPPER.readValue(JsonValue.readHjson(body).toString(), UmaResourceRegistrationRequest.class);
            if (umaRequest == null) {
                val model = buildResponseEntityErrorModel(HttpStatus.NOT_FOUND, "UMA request cannot be found or parsed");
                return new ResponseEntity(model, model, HttpStatus.BAD_REQUEST);
            }

            val resourceSet = umaRequest.asResourceSet(profileResult);
            resourceSet.validate(profileResult);

            val saved = getUmaConfigurationContext().getUmaResourceSetRepository().save(resourceSet);
            val location = getResourceSetUriLocation(saved);

            val model = CollectionUtils.wrap("entity", saved,
                "code", HttpStatus.CREATED,
                "resourceId", saved.getId(),
                "location", location);
            return new ResponseEntity(model, HttpStatus.OK);
        } catch (final InvalidResourceSetException e) {
            return new ResponseEntity(buildResponseEntityErrorModel(e), HttpStatus.BAD_REQUEST);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity("Unable to complete the resource-set registration request.", HttpStatus.BAD_REQUEST);
    }
}
