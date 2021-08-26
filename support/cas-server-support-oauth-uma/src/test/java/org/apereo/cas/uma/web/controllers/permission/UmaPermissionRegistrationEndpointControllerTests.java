package org.apereo.cas.uma.web.controllers.permission;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
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
public class UmaPermissionRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyPermissionRegistrationOperation() throws Exception {
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
    public void verifyFailsAuthn() throws Exception {
        val body = createUmaPermissionRegistrationRequest(100).toJson();
        val response = umaPermissionRegistrationEndpointController.handle(body,
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void verifyBadInput() throws Exception {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = "###";
        var response = umaPermissionRegistrationEndpointController.handle(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void verifyBadProfile() throws Exception {
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
        commonProfile.setPermissions(Set.of(OAuth20Constants.UMA_PROTECTION_SCOPE));
        manager.save(true, commonProfile, false);

        response = umaPermissionRegistrationEndpointController.handle(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
