package org.apereo.cas.uma.web.controllers.policy;

import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaFindPolicyForResourceSetEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
public class UmaFindPolicyForResourceSetEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyOperation() throws Exception {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());

        var model = (Map) response.getBody();
        assertNotNull(model);
        val resourceId = (long) model.get("resourceId");

        body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();
        umaCreatePolicyForResourceSetEndpointController.createPolicyForResourceSet(resourceId, body, results.getLeft(), results.getMiddle());

        response = umaFindPolicyForResourceSetEndpointController.getPoliciesForResourceSet(resourceId, results.getLeft(), results.getMiddle());
        model = (Map) response.getBody();
        assertNotNull(model);
        val policyId = ((Collection<ResourceSetPolicy>) model.get("entity")).iterator().next().getId();

        response = umaFindPolicyForResourceSetEndpointController.getPolicyForResourceSet(resourceId,
            policyId, results.getLeft(), results.getMiddle());
        model = (Map) response.getBody();
        assertNotNull(model);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));
    }

    @Test
    public void verifyMissingPolicyOperation() throws Exception {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());

        var model = (Map) response.getBody();
        assertNotNull(model);
        val resourceId = (long) model.get("resourceId");

        body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();
        umaCreatePolicyForResourceSetEndpointController.createPolicyForResourceSet(resourceId, body, results.getLeft(), results.getMiddle());

        response = umaFindPolicyForResourceSetEndpointController.getPoliciesForResourceSet(resourceId, results.getLeft(), results.getMiddle());
        model = (Map) response.getBody();
        assertNotNull(model);
        response = umaFindPolicyForResourceSetEndpointController.getPolicyForResourceSet(resourceId,
            123456, results.getLeft(), results.getMiddle());
        model = (Map) response.getBody();
        assertNotNull(model);
        assertTrue(model.containsKey("code"));
        assertFalse(model.containsKey("entity"));
    }

    @Test
    public void verifyMissingOperation() throws Exception {
        val results = authenticateUmaRequestWithProtectionScope();
        var response = umaFindPolicyForResourceSetEndpointController.getPoliciesForResourceSet(10, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = umaFindPolicyForResourceSetEndpointController.getPolicyForResourceSet(10, 100, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void verifyNoAuthOperation() throws Exception {
        var response = umaFindPolicyForResourceSetEndpointController.getPoliciesForResourceSet(10,
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        response = umaFindPolicyForResourceSetEndpointController.getPolicyForResourceSet(10, 100,
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
