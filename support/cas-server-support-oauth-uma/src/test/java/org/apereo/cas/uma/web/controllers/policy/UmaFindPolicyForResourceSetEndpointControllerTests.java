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
 * This is {@link UmaFindPolicyForResourceSetEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
class UmaFindPolicyForResourceSetEndpointControllerTests extends BaseUmaEndpointControllerTests {
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
        performUmaRequest(HttpMethod.POST, resourceId + "/" + OAuth20Constants.UMA_POLICY_URL,
            body, results.getLeft(), results.getMiddle());

        result = performUmaRequest(HttpMethod.GET, resourceId + "/" + OAuth20Constants.UMA_POLICY_URL,
            results.getLeft(), results.getMiddle());
        model = getMappedResponseBody(result);
        assertNotNull(model);
        val policy = (Map) ((Collection) model.get("entity")).iterator().next();
        val policyId = ((Number) policy.get("id")).longValue();

        result = performUmaRequest(HttpMethod.GET,
            resourceId + "/" + OAuth20Constants.UMA_POLICY_URL + "/" + policyId,
            results.getLeft(), results.getMiddle());
        model = getMappedResponseBody(result);
        assertNotNull(model);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));
    }

    @Test
    void verifyMissingPolicyOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());

        var model = getMappedResponseBody(result);
        assertNotNull(model);
        val resourceId = ((Number) model.get("resourceId")).longValue();

        body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();
        performUmaRequest(HttpMethod.POST, resourceId + "/" + OAuth20Constants.UMA_POLICY_URL,
            body, results.getLeft(), results.getMiddle());

        result = performUmaRequest(HttpMethod.GET, resourceId + "/" + OAuth20Constants.UMA_POLICY_URL,
            results.getLeft(), results.getMiddle());
        model = getMappedResponseBody(result);
        assertNotNull(model);
        result = performUmaRequest(HttpMethod.GET,
            resourceId + "/" + OAuth20Constants.UMA_POLICY_URL + "/123456",
            results.getLeft(), results.getMiddle());
        model = getMappedResponseBody(result);
        assertNotNull(model);
        assertTrue(model.containsKey("code"));
        assertFalse(model.containsKey("entity"));
    }

    @Test
    void verifyMissingOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var result = performUmaRequest(HttpMethod.GET, "10/" + OAuth20Constants.UMA_POLICY_URL,
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        result = performUmaRequest(HttpMethod.GET, "10/" + OAuth20Constants.UMA_POLICY_URL + "/100",
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyNoAuthOperation() throws Throwable {
        var result = performUmaRequest(HttpMethod.GET, "10/" + OAuth20Constants.UMA_POLICY_URL);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
        result = performUmaRequest(HttpMethod.GET, "10/" + OAuth20Constants.UMA_POLICY_URL + "/100");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }
}
