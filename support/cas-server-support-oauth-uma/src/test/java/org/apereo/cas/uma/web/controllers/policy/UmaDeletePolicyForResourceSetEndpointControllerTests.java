package org.apereo.cas.uma.web.controllers.policy;

import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaDeletePolicyForResourceSetEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaDeletePolicyForResourceSetEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyOperation() throws Exception {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();

        response = umaCreatePolicyForResourceSetEndpointController.createPolicyForResourceSet(resourceId, body, results.getLeft(), results.getMiddle());
        model = (Map) response.getBody();
        val policyId = ((ResourceSet) model.get("entity")).getPolicies().iterator().next().getId();

        response = umaDeletePolicyForResourceSetEndpointController.deletePoliciesForResourceSet(resourceId, policyId, results.getLeft(), results.getMiddle());
        model = (Map) response.getBody();
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));
    }
}
