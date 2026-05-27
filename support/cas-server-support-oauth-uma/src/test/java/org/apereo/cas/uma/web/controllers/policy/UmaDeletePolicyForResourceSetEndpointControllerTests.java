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
 * This is {@link UmaDeletePolicyForResourceSetEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
class UmaDeletePolicyForResourceSetEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    void verifyOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        var model = getMappedResponseBody(result);
        val resourceId = ((Number) model.get("resourceId")).longValue();

        body = createUmaPolicyRegistrationRequest(getCurrentProfile(results.getLeft(), results.getMiddle())).toJson();

        result = performUmaRequest(HttpMethod.POST, resourceId + "/" + OAuth20Constants.UMA_POLICY_URL,
            body, results.getLeft(), results.getMiddle());
        model = getMappedResponseBody(result);
        val entity = (Map) model.get("entity");
        val policyId = ((Number) ((Map) ((Collection) entity.get("policies")).iterator().next()).get("id")).longValue();

        result = performUmaRequest(HttpMethod.DELETE,
            resourceId + "/" + OAuth20Constants.UMA_POLICY_URL + "/" + policyId,
            results.getLeft(), results.getMiddle());
        model = getMappedResponseBody(result);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));
    }

    @Test
    void verifyFailsOperation() throws Throwable {
        val result = performUmaRequest(HttpMethod.DELETE, "1/" + OAuth20Constants.UMA_POLICY_URL + "/1");
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyMissingOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        val result = performUmaRequest(HttpMethod.DELETE, "1/" + OAuth20Constants.UMA_POLICY_URL + "/2",
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }
}
