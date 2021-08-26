package org.apereo.cas.uma.web.controllers.authz;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaAuthorizationRequestEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
public class UmaAuthorizationRequestEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyPermTicketNoPolicy() {
        val permissionTicket = getPermissionTicketWith(List.of("read", "write"));

        var results = authenticateUmaRequestWithAuthorizationScope();

        val authzRequest = new UmaAuthorizationRequest();
        authzRequest.setGrantType(OAuth20GrantTypes.UMA_TICKET.getType());
        authzRequest.setTicket(permissionTicket);
        var body = authzRequest.toJson();

        val ticket = ticketRegistry.getTicket(permissionTicket, UmaPermissionTicket.class);
        ticket.getResourceSet().setPolicies(new HashSet<>());
        ticketRegistry.updateTicket(ticket);

        var response = umaAuthorizationRequestEndpointController.handleAuthorizationRequest(body,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void verifyAuthorizationOperation() {
        val permissionTicket = getPermissionTicketWith(List.of("read", "write"));

        var results = authenticateUmaRequestWithAuthorizationScope();

        val authzRequest = new UmaAuthorizationRequest();
        authzRequest.setGrantType(OAuth20GrantTypes.UMA_TICKET.getType());
        authzRequest.setTicket(permissionTicket);
        var body = authzRequest.toJson();
        var response = umaAuthorizationRequestEndpointController.handleAuthorizationRequest(body,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var model = (Map) response.getBody();
        assertNotNull(model);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("rpt"));
    }

    @Test
    public void verifyMismatchedClaims() {
        val permissionTicket = getPermissionTicketWith(List.of("delete", "open"));
        val results = authenticateUmaRequestWithAuthorizationScope();

        val authzRequest = new UmaAuthorizationRequest()
            .setGrantType(OAuth20GrantTypes.UMA_TICKET.getType())
            .setTicket(permissionTicket)
            .toJson();

        val permission = new ResourceSetPolicyPermission();
        permission.getClaims().put("lastName", "Apereo");

        val ticket = ticketRegistry.getTicket(permissionTicket, UmaPermissionTicket.class);
        ticket.getResourceSet().getScopes().add("hello");
        val resourceSetPolicy = new ResourceSetPolicy().setId(2000);
        resourceSetPolicy.getPermissions().add(permission);
        ticket.getResourceSet().getPolicies().add(resourceSetPolicy);
        val response = umaAuthorizationRequestEndpointController.handleAuthorizationRequest(authzRequest,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.PERMANENT_REDIRECT, response.getStatusCode());
    }

    @Test
    public void verifyMissingGrant() {
        var results = authenticateUmaRequestWithAuthorizationScope();
        var authzRequest = new UmaAuthorizationRequest().toJson();
        var response = umaAuthorizationRequestEndpointController.handleAuthorizationRequest(authzRequest,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authzRequest = new UmaAuthorizationRequest()
            .setGrantType(null)
            .toJson();
        response = umaAuthorizationRequestEndpointController.handleAuthorizationRequest(authzRequest,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authzRequest = new UmaAuthorizationRequest()
            .setGrantType("unknown")
            .toJson();
        response = umaAuthorizationRequestEndpointController.handleAuthorizationRequest(authzRequest,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authzRequest = new UmaAuthorizationRequest()
            .setGrantType(OAuth20GrantTypes.UMA_TICKET.getType())
            .setTicket(null)
            .toJson();
        response = umaAuthorizationRequestEndpointController.handleAuthorizationRequest(authzRequest, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        authzRequest = new UmaAuthorizationRequest()
            .setGrantType(OAuth20GrantTypes.UMA_TICKET.getType())
            .setTicket("unknown-ticket")
            .toJson();
        response = umaAuthorizationRequestEndpointController.handleAuthorizationRequest(authzRequest, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private String getPermissionTicketWith(final List<String> scopes) {
        var results = authenticateUmaRequestWithProtectionScope();

        var body = createUmaResourceRegistrationRequest(1000, scopes).toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        var model = (Map) response.getBody();
        assertNotNull(model);
        val resourceId = (long) model.get("resourceId");

        val profile = getCurrentProfile(results.getLeft(), results.getMiddle());
        body = createUmaPolicyRegistrationRequest(profile, scopes).toJson();

        response = umaCreatePolicyForResourceSetEndpointController.createPolicyForResourceSet(resourceId,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        body = createUmaPermissionRegistrationRequest(resourceId).toJson();
        response = umaPermissionRegistrationEndpointController.handle(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        model = (Map) response.getBody();
        assertNotNull(model);
        return model.get("ticket").toString();
    }

}
