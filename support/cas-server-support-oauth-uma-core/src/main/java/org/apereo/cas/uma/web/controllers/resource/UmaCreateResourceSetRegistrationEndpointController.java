package org.apereo.cas.uma.web.controllers.resource;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointController;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link UmaCreateResourceSetRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Tag(name = "User Managed Access")
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
    @PostMapping(OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL)
    @Operation(
        summary = "Register resource set",
        description = "Registers a resource set and returns the resource set ID"
    )
    public ResponseEntity registerResourceSet(
        @RequestBody
        final String body,
        final HttpServletRequest request, final HttpServletResponse response) {
        try {
            val profileResult = getAuthenticatedProfile(request, response, OAuth20Constants.UMA_PROTECTION_SCOPE);

            val umaRequest = MAPPER.readValue(JsonValue.readHjson(body).toString(), UmaResourceRegistrationRequest.class);
            if (umaRequest == null || StringUtils.isBlank(umaRequest.getName())) {
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
            return new ResponseEntity<>(model, HttpStatus.OK);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ResponseEntity<>("Unable to complete the resource-set registration request.", HttpStatus.BAD_REQUEST);
    }
}
