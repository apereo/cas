package org.apereo.cas.uma.web.controllers.resource;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Set;

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
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        assertNotNull(response.getBody());
        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        response = umaDeleteResourceSetRegistrationEndpointController.deleteResourceSet(resourceId, results.getLeft(), results.getMiddle());
        assertNotNull(response.getBody());

        model = (Map) response.getBody();
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("resourceId"));
    }

    @Test
    void verifyEmpty() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var response = umaDeleteResourceSetRegistrationEndpointController.deleteResourceSet(-1, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void verifyBadClientId() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        assertNotNull(response.getBody());
        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        val context = new JEEContext(results.getLeft(), results.getMiddle());
        val manager = new ProfileManager(context, oauthDistributedSessionStore);
        manager.removeProfiles();
        response = umaDeleteResourceSetRegistrationEndpointController.deleteResourceSet(resourceId,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        val commonProfile = new CommonProfile();
        commonProfile.setClientName("CasClient");
        commonProfile.setId("testuser");
        commonProfile.setRoles(Set.of(OAuth20Constants.UMA_PROTECTION_SCOPE));
        manager.save(true, commonProfile, false);
        response = umaDeleteResourceSetRegistrationEndpointController.deleteResourceSet(resourceId,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
