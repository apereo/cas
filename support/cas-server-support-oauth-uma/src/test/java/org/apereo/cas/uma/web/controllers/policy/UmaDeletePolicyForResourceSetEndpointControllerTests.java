package org.apereo.cas.uma.web.controllers.policy;

import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaDeletePolicyForResourceSetEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
public class UmaDeletePolicyForResourceSetEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyOperation() throws Exception {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();

        response = umaCreatePolicyForResourceSetEndpointController.createPolicyForResourceSet(resourceId, body, results.getLeft(),
            results.getMiddle());
        model = (Map) response.getBody();
        val policyId = ((ResourceSet) model.get("entity")).getPolicies().iterator().next().getId();

        response = umaDeletePolicyForResourceSetEndpointController.deletePoliciesForResourceSet(resourceId, policyId, results.getLeft(),
            results.getMiddle());
        model = (Map) response.getBody();
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));
    }

    @Test
    public void verifyFailsOperation() throws Exception {
        val response = umaDeletePolicyForResourceSetEndpointController.deletePoliciesForResourceSet(1, 1,
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void verifyMissingOperation() throws Exception {
        val results = authenticateUmaRequestWithProtectionScope();
        val response = umaDeletePolicyForResourceSetEndpointController.deletePoliciesForResourceSet(1, 2,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
