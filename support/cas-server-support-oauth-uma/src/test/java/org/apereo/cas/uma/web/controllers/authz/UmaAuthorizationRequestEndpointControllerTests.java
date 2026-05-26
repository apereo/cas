package org.apereo.cas.uma.web.controllers.authz;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaAuthorizationRequestEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
class UmaAuthorizationRequestEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    void verifyPermTicketNoPolicy() throws Throwable {
        val permissionTicket = getPermissionTicketWith(List.of("read", "write"));

        var results = authenticateUmaRequestWithAuthorizationScope();

        val authzRequest = new UmaAuthorizationRequest();
        authzRequest.setGrantType(OAuth20GrantTypes.UMA_TICKET.getType());
        authzRequest.setTicket(permissionTicket);
        var body = authzRequest.toJson();

        val ticket = ticketRegistry.getTicket(permissionTicket, UmaPermissionTicket.class);
        Objects.requireNonNull(ticket.getResourceSet()).setPolicies(new HashSet<>());
        ticketRegistry.updateTicket(ticket);

        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyAuthorizationOperation() throws Throwable {
        val permissionTicket = getPermissionTicketWith(List.of("read", "write"));

        var results = authenticateUmaRequestWithAuthorizationScope();

        val authzRequest = new UmaAuthorizationRequest();
        authzRequest.setGrantType(OAuth20GrantTypes.UMA_TICKET.getType());
        authzRequest.setTicket(permissionTicket);
        var body = authzRequest.toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        var model = getMappedResponseBody(result);
        assertNotNull(model);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("rpt"));
    }

    @Test
    void verifyMismatchedClaims() throws Throwable {
        val permissionTicket = getPermissionTicketWith(List.of("delete", "open"));
        val results = authenticateUmaRequestWithAuthorizationScope();

        val authzRequest = new UmaAuthorizationRequest()
            .setGrantType(OAuth20GrantTypes.UMA_TICKET.getType())
            .setTicket(permissionTicket)
            .toJson();

        val permission = new ResourceSetPolicyPermission();
        permission.getClaims().put("lastName", "Apereo");

        val ticket = ticketRegistry.getTicket(permissionTicket, UmaPermissionTicket.class);
        val resourceSet = Objects.requireNonNull(ticket.getResourceSet());
        resourceSet.getScopes().add("hello");
        val resourceSetPolicy = new ResourceSetPolicy().setId(2000);
        resourceSetPolicy.getPermissions().add(permission);
        resourceSet.getPolicies().add(resourceSetPolicy);
        val result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL,
            authzRequest, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyMissingGrant() throws Throwable {
        var results = authenticateUmaRequestWithAuthorizationScope();
        var authzRequest = new UmaAuthorizationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL,
            authzRequest, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());

        authzRequest = new UmaAuthorizationRequest()
            .setGrantType(null)
            .toJson();
        result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL,
            authzRequest, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());

        authzRequest = new UmaAuthorizationRequest()
            .setGrantType("unknown")
            .toJson();
        result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL,
            authzRequest, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());

        authzRequest = new UmaAuthorizationRequest()
            .setGrantType(OAuth20GrantTypes.UMA_TICKET.getType())
            .setTicket(null)
            .toJson();
        result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL,
            authzRequest, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());

        authzRequest = new UmaAuthorizationRequest()
            .setGrantType(OAuth20GrantTypes.UMA_TICKET.getType())
            .setTicket("unknown-ticket")
            .toJson();
        result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL,
            authzRequest, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    private String getPermissionTicketWith(final List<String> scopes) throws Throwable {
        var results = authenticateUmaRequestWithProtectionScope();

        var body = createUmaResourceRegistrationRequest(RandomUtils.nextInt(), scopes).toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        var model = getMappedResponseBody(result);
        assertNotNull(model);
        val resourceId = ((Number) model.get("resourceId")).longValue();

        val profile = getCurrentProfile(results.getLeft(), results.getMiddle());
        body = createUmaPolicyRegistrationRequest(profile, scopes).toJson();

        result = performUmaRequest(HttpMethod.POST, resourceId + "/" + OAuth20Constants.UMA_POLICY_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        body = createUmaPermissionRegistrationRequest(resourceId).toJson();
        result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_PERMISSION_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        model = getMappedResponseBody(result);
        assertNotNull(model);
        return model.get("ticket").toString();
    }

}
