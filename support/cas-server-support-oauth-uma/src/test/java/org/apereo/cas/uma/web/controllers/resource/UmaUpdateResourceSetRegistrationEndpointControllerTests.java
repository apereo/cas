package org.apereo.cas.uma.web.controllers.resource;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaUpdateResourceSetRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
class UmaUpdateResourceSetRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {

    @Test
    void verifyRegistrationOperation() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        var model = getMappedResponseBody(result);
        val resourceId = ((Number) model.get("resourceId")).longValue();

        body = createUmaResourceRegistrationRequest(resourceId).toJson();

        result = performUmaRequest(HttpMethod.PUT,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/" + resourceId,
            body, results.getLeft(), results.getMiddle());
        model = getMappedResponseBody(result);
        assertTrue(model.containsKey("entity"));
        assertTrue(model.containsKey("location"));
        assertTrue(model.containsKey("resourceId"));
    }

    @Test
    void verifyFailsId() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();

        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        var model = getMappedResponseBody(result);
        val resourceId = ((Number) model.get("resourceId")).longValue();

        body = createUmaResourceRegistrationRequest(resourceId).toJson();

        result = performUmaRequest(HttpMethod.PUT,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/-1",
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyFailsMissing() throws Throwable {
        val id = RandomUtils.nextLong();
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest(id).toJson();
        val result = performUmaRequest(HttpMethod.PUT,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/" + id,
            body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyNoAuth() throws Throwable {
        var body = createUmaResourceRegistrationRequest(1000).toJson();
        val result = performUmaRequest(HttpMethod.PUT,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/1000", body);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }
}
