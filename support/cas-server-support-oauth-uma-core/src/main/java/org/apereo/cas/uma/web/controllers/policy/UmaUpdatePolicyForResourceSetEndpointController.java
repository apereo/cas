package org.apereo.cas.uma.web.controllers.policy;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointController;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * This is {@link UmaUpdatePolicyForResourceSetEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Controller("umaUpdatePolicyForResourceSetEndpointController")
public class UmaUpdatePolicyForResourceSetEndpointController extends BaseUmaEndpointController {
    public UmaUpdatePolicyForResourceSetEndpointController(final UmaConfigurationContext umaConfigurationContext) {
        super(umaConfigurationContext);
    }

    /**
     * Update policy for resource set.
     *
     * @param resourceId the resource id
     * @param policyId   the policy id
     * @param body       the body
     * @param request    the request
     * @param response   the response
     * @return the policy for resource set
     */
    @PutMapping(value = '/' + OAuth20Constants.BASE_OAUTH20_URL + "/{resourceId}/" + OAuth20Constants.UMA_POLICY_URL + "/{policyId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updatePoliciesForResourceSet(@PathVariable(value = "resourceId") final long resourceId,
                                                       @PathVariable(value = "policyId") final long policyId,
                                                       @RequestBody final String body,
                                                       final HttpServletRequest request,
                                                       final HttpServletResponse response) {
        try {
            val profileResult = getAuthenticatedProfile(request, response, OAuth20Constants.UMA_PROTECTION_SCOPE);
            val resourceSetResult = getUmaConfigurationContext().getUmaResourceSetRepository().getById(resourceId);
            if (resourceSetResult.isEmpty()) {
                val model = buildResponseEntityErrorModel(HttpStatus.NOT_FOUND, "Requested resource-set cannot be found");
                return new ResponseEntity(model, model, HttpStatus.BAD_REQUEST);
            }
            val resourceSet = resourceSetResult.get();
            resourceSet.validate(profileResult);

            val umaRequest = MAPPER.readValue(JsonValue.readHjson(body).toString(), ResourceSetPolicy.class);
            if (umaRequest == null) {
                val model = buildResponseEntityErrorModel(HttpStatus.NOT_FOUND, "UMA policy request cannot be found or parsed");
                return new ResponseEntity(model, model, HttpStatus.BAD_REQUEST);
            }

            val policyResult = resourceSet.getPolicies().stream().filter(p -> p.getId() == policyId).findFirst();
            if (policyResult.isPresent()) {
                val policy = policyResult.get();
                policy.setPermissions(umaRequest.getPermissions());

                val currentPolicies = resourceSet.getPolicies().stream().filter(p -> p.getId() != policyId).collect(Collectors.toSet());
                currentPolicies.add(policy);
                resourceSet.setPolicies(new HashSet<>(currentPolicies));
                getUmaConfigurationContext().getUmaResourceSetRepository().save(resourceSet);

                val model = CollectionUtils.wrap("entity", resourceSet, "code", HttpStatus.FOUND);
                return new ResponseEntity(model, HttpStatus.OK);
            }

            val model = CollectionUtils.wrap("code", HttpStatus.NOT_FOUND);
            return new ResponseEntity(model, HttpStatus.OK);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity("Unable to locate resource-set.", HttpStatus.BAD_REQUEST);
    }

}
