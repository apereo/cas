package org.apereo.cas.uma.web.controllers.resource;

import module java.base;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaFindResourceSetRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
@Execution(ExecutionMode.SAME_THREAD)
class UmaFindResourceSetRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {
    
    @Test
    void verifyOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());

        val response = umaFindResourceSetRegistrationEndpointController.findResourceSets(results.getLeft(), results.getMiddle());
        assertNotNull(response.getBody());
        val model = (Collection) response.getBody();
        assertEquals(1, model.size());
    }

    @Test
    void verifyUnAuthOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        assertNotNull(response.getBody());

        val context = new JEEContext(results.getLeft(), results.getMiddle());
        val manager = new ProfileManager(context, oauthDistributedSessionStore);
        manager.removeProfiles();

        response = umaFindResourceSetRegistrationEndpointController.findResourceSets(results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void verifyFailsToFind() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        assertNotNull(response.getBody());
        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        response = umaFindResourceSetRegistrationEndpointController.findResourceSet(-1, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        val context = new JEEContext(results.getLeft(), results.getMiddle());
        val manager = new ProfileManager(context, oauthDistributedSessionStore);
        manager.removeProfiles();

        response = umaFindResourceSetRegistrationEndpointController.findResourceSet(resourceId, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
