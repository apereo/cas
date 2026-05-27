package org.apereo.cas.uma.web.controllers.permission;

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
 * This is {@link UmaPermissionRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
class UmaPermissionRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    void verifyPermissionRegistrationOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        val body = createUmaPermissionRegistrationRequest(100).toJson();
        val result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_PERMISSION_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        val model = getMappedResponseBody(result);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("message"));
    }

    @Test
    void verifyFailsAuthn() throws Throwable {
        val body = createUmaPermissionRegistrationRequest(100).toJson();
        val result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_PERMISSION_URL, body);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyBadInput() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = "###";
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_PERMISSION_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyBadProfile() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();

        var body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());

        var model = getMappedResponseBody(result);
        val resourceId = ((Number) model.get("resourceId")).longValue();

        val profile = getCurrentProfile(results.getLeft(), results.getMiddle());
        body = createUmaPolicyRegistrationRequest(profile).toJson();

        result = performUmaRequest(HttpMethod.POST, resourceId + "/" + OAuth20Constants.UMA_POLICY_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        val resourceSet = umaResourceSetRepository.getById(resourceId).orElseThrow();
        resourceSet.setOwner("testuser");
        umaResourceSetRepository.save(resourceSet);

        body = createUmaPermissionRegistrationRequest(resourceId).toJson();
        result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_PERMISSION_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }
}
