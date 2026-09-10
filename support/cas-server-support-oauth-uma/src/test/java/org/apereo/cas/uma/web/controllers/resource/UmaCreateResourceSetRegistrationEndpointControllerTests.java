package org.apereo.cas.uma.web.controllers.resource;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaCreateResourceSetRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
class UmaCreateResourceSetRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {

    @Test
    void verifyFailsNoAuth() throws Throwable {
        var body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL, body);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyBadInput() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body ="###";
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyRegistrationOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        var model = getMappedResponseBody(result);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("location"));
        assertTrue(model.containsKey("entity"));
        assertTrue(model.containsKey("resourceId"));

        val resourceId = parseIdentifier(model.get("resourceId"));
        val entity = (Map) model.get("entity");
        assertEquals(String.valueOf(resourceId), entity.get("id"));
        result = performUmaRequest(HttpMethod.GET,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + '/' + resourceId,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        model = getMappedResponseBody(result);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));

        body = createUmaPermissionRegistrationRequest(resourceId).toJson();
        result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_PERMISSION_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        model = getMappedResponseBody(result);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("ticket"));

    }

    @Test
    void verifyResourceIdentifierIsAssignedByServer() throws Throwable {
        val requestedId = 12345L;
        val results = authenticateUmaRequestWithProtectionScope();
        val body = createUmaResourceRegistrationRequest(requestedId).toJson();
        val result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        val model = getMappedResponseBody(result);
        assertNotEquals(requestedId, parseIdentifier(model.get("resourceId")));
    }
}
