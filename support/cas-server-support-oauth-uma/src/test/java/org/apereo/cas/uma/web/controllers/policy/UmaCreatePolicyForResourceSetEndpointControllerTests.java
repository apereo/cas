package org.apereo.cas.uma.web.controllers.policy;

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
 * This is {@link UmaCreatePolicyForResourceSetEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
class UmaCreatePolicyForResourceSetEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    void verifyOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());

        var model = getMappedResponseBody(result);
        assertNotNull(model);
        val resourceId = ((Number) model.get("resourceId")).longValue();

        body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();

        result = performUmaRequest(HttpMethod.POST, resourceId + "/" + OAuth20Constants.UMA_POLICY_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        model = getMappedResponseBody(result);
        assertNotNull(model);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));
    }

    @Test
    void verifyMissingOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        val body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();
        val result = performUmaRequest(HttpMethod.POST, 210 + "/" + OAuth20Constants.UMA_POLICY_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyNoAuthOperation() throws Throwable {
        val body = createUmaResourceRegistrationRequest().toJson();
        val result = performUmaRequest(HttpMethod.POST, 100 + "/" + OAuth20Constants.UMA_POLICY_URL, body);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }
}
