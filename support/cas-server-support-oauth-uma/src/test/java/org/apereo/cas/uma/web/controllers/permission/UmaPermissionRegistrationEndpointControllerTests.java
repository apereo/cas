package org.apereo.cas.uma.web.controllers.permission;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;
import java.util.Set;

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
        val response = umaPermissionRegistrationEndpointController.handle(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        val model = (Map) response.getBody();
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("message"));
    }

    @Test
    void verifyFailsAuthn() {
        val body = createUmaPermissionRegistrationRequest(100).toJson();
        val response = umaPermissionRegistrationEndpointController.handle(body,
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void verifyBadInput() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = "###";
        var response = umaPermissionRegistrationEndpointController.handle(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void verifyBadProfile() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();

        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());

        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        val profile = getCurrentProfile(results.getLeft(), results.getMiddle());
        body = createUmaPolicyRegistrationRequest(profile).toJson();

        response = umaCreatePolicyForResourceSetEndpointController.createPolicyForResourceSet(resourceId,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        body = createUmaPermissionRegistrationRequest(resourceId).toJson();

        val context = new JEEContext(results.getLeft(), results.getMiddle());
        val manager = new ProfileManager(context, oauthDistributedSessionStore);
        manager.removeProfiles();

        val commonProfile = new CommonProfile();
        commonProfile.setClientName("CasClient");
        commonProfile.setId("testuser");
        commonProfile.setRoles(Set.of(OAuth20Constants.UMA_PROTECTION_SCOPE));
        manager.save(true, commonProfile, false);

        response = umaPermissionRegistrationEndpointController.handle(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
