package org.apereo.cas.uma.web.controllers.resource;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpMethod;
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
        performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());

        val result = performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            results.getLeft(), results.getMiddle());
        val model = MAPPER.readValue(result.getResponse().getContentAsByteArray(), Collection.class);
        assertNotNull(model);
    }

    @Test
    void verifyUnAuthOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());

        val context = new JEEContext(results.getLeft(), results.getMiddle());
        val manager = new ProfileManager(context, oauthDistributedSessionStore);
        manager.removeProfiles();

        val result = performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyFailsToFind() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        var model = getMappedResponseBody(result);
        val resourceId = ((Number) model.get("resourceId")).longValue();

        result = performUmaRequest(HttpMethod.GET,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/-1",
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());

        val context = new JEEContext(results.getLeft(), results.getMiddle());
        val manager = new ProfileManager(context, oauthDistributedSessionStore);
        manager.removeProfiles();

        result = performUmaRequest(HttpMethod.GET,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/" + resourceId,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }
}
