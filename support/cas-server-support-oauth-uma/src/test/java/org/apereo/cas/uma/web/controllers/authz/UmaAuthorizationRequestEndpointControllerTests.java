package org.apereo.cas.uma.web.controllers.authz;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Test;
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
        model = (Map) response.getBody();

        val permissionTicket = model.get("ticket").toString();

        results = authenticateUmaRequestWithAuthorizationScope();

        val authzRequest = new UmaAuthorizationRequest();
        authzRequest.setGrantType(OAuth20GrantTypes.UMA_TICKET.getType());
        authzRequest.setTicket(permissionTicket);
        body = authzRequest.toJson();
        response = umaAuthorizationRequestEndpointController.handleAuthorizationRequest(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        model = (Map) response.getBody();
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("rpt"));
    }
}
