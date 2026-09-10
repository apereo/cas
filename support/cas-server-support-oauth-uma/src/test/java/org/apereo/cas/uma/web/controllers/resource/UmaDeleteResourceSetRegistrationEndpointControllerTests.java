package org.apereo.cas.uma.web.controllers.resource;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaDeleteResourceSetRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
class UmaDeleteResourceSetRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {

    @Test
    void verifyOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        var model = getMappedResponseBody(result);
        val resourceId = parseIdentifier(model.get("resourceId"));

        result = performUmaRequest(HttpMethod.DELETE,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + '/' + resourceId,
            results.getLeft(), results.getMiddle());
        model = getMappedResponseBody(result);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("resourceId"));
    }

    @Test
    void verifyEmpty() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        val result = performUmaRequest(HttpMethod.DELETE,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/-1",
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyResourceCannotBeDeletedByDifferentClientWithSameOwner() throws Throwable {
        val resourceOwner = authenticateUmaRequestWithProtectionScope();
        val body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, resourceOwner.getLeft(), resourceOwner.getMiddle());
        val model = getMappedResponseBody(result);
        val resourceId = parseIdentifier(model.get("resourceId"));

        val otherClient = authenticateUmaRequestWithProtectionScope();
        val ownerProfile = getCurrentProfile(resourceOwner.getLeft(), resourceOwner.getMiddle());
        val otherClientProfile = getCurrentProfile(otherClient.getLeft(), otherClient.getMiddle());
        assertEquals(ownerProfile.getId(), otherClientProfile.getId());
        assertNotEquals(OAuth20Utils.getClientIdFromAuthenticatedProfile(ownerProfile),
            OAuth20Utils.getClientIdFromAuthenticatedProfile(otherClientProfile));

        result = performUmaRequest(HttpMethod.DELETE,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + '/' + resourceId,
            otherClient.getLeft(), otherClient.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertTrue(umaResourceSetRepository.getById(resourceId).isPresent());
    }
}
