package org.apereo.cas.uma.web.controllers.authz;

import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link UmaAuthorizationRequestEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaAuthorizationRequestEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyAuthorizationOperation() throws Exception {
        var results = authenticateUmaRequestWithProtectionScope();

        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        val profile = getCurrentProfile(results.getLeft(), results.getMiddle());
        body = createUmaPolicyRegistrationRequest(profile).toJson();

        response = umaCreatePolicyForResourceSetEndpointController.createPolicyForResourceSet(resourceId, body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        body = createUmaPermissionRegistrationRequest(resourceId).toJson();
        response = umaPermissionRegistrationEndpointController.handle(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Execute authorization
        results = authenticateUmaRequestWithAuthorizationScope();

//        val authzRequest = new UmaAuthorizationRequest();
//        umaAuthorizationRequestEndpointController.handleAuthorizationRequest(body, results.getLeft(), results.getMiddle());

    }
}
