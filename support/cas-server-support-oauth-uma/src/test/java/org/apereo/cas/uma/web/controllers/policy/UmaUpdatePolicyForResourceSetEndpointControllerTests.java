package org.apereo.cas.uma.web.controllers.policy;

import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaUpdatePolicyForResourceSetEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
public class UmaUpdatePolicyForResourceSetEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyOperation() {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body,
            results.getLeft(), results.getMiddle());
        var model = (Map) response.getBody();
        assertNotNull(model);
        val resourceId = (long) model.get("resourceId");

        body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();
        response = umaCreatePolicyForResourceSetEndpointController.createPolicyForResourceSet(resourceId, body,
            results.getLeft(), results.getMiddle());
        model = (Map) response.getBody();
        assertNotNull(model);
        val policyId = ((ResourceSet) model.get("entity")).getPolicies().iterator().next().getId();

        body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle()),
            CollectionUtils.wrapHashSet("read")).toJson();
        response = umaUpdatePolicyForResourceSetEndpointController.updatePoliciesForResourceSet(resourceId, policyId, body,
            results.getLeft(), results.getMiddle());
        model = (Map) response.getBody();
        assertNotNull(model);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));
    }

    @Test
    public void verifyNoAuth() {
        var body = createUmaResourceRegistrationRequest().toJson();
        val response = umaUpdatePolicyForResourceSetEndpointController.updatePoliciesForResourceSet(1, 2, body,
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void verifyMissingChannel() {
        var results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body,
            results.getLeft(), results.getMiddle());
        var model = (Map) response.getBody();
        assertNotNull(model);
        val resourceId = (long) model.get("resourceId");

        body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();
        results = authenticateUmaRequestWithProtectionScope();
        response = umaUpdatePolicyForResourceSetEndpointController.updatePoliciesForResourceSet(resourceId, 2, body,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void verifyMissingResource() {
        var results = authenticateUmaRequestWithProtectionScope();
        val body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();
        results = authenticateUmaRequestWithProtectionScope();
        val response = umaUpdatePolicyForResourceSetEndpointController.updatePoliciesForResourceSet(123, 2, body,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
