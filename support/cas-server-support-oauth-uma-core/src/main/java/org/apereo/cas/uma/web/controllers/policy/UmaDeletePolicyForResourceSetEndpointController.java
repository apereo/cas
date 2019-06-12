package org.apereo.cas.uma.web.controllers.policy;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointController;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * This is {@link UmaDeletePolicyForResourceSetEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Controller("umaDeletePolicyForResourceSetEndpointController")
public class UmaDeletePolicyForResourceSetEndpointController extends BaseUmaEndpointController {
    public UmaDeletePolicyForResourceSetEndpointController(final UmaConfigurationContext umaConfigurationContext) {
        super(umaConfigurationContext);
    }

    /**
     * Delete policy for resource set.
     *
     * @param resourceId the resource id
     * @param policyId   the policy id
     * @param request    the request
     * @param response   the response
     * @return the policy for resource set
     */
    @DeleteMapping(value = '/' + OAuth20Constants.BASE_OAUTH20_URL + "/{resourceId}/" + OAuth20Constants.UMA_POLICY_URL + "/{policyId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deletePoliciesForResourceSet(@PathVariable(value = "resourceId") final long resourceId,
                                                       @PathVariable(value = "policyId") final long policyId,
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

            val policies = resourceSet.getPolicies().stream().filter(p -> p.getId() != policyId).collect(Collectors.toSet());
            resourceSet.setPolicies(new HashSet<>(policies));
            val saved = getUmaConfigurationContext().getUmaResourceSetRepository().save(resourceSet);

            val model = CollectionUtils.wrap("entity", saved, "code", HttpStatus.OK);
            return new ResponseEntity(model, HttpStatus.OK);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity("Unable to locate resource-set.", HttpStatus.BAD_REQUEST);
    }
}
