package org.apereo.cas.uma.web.controllers.resource;

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
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        var model = getMappedResponseBody(result);
        val resourceId = ((Number) model.get("resourceId")).longValue();

        result = performUmaRequest(HttpMethod.DELETE,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/" + resourceId,
            results.getLeft(), results.getMiddle());
        model = getMappedResponseBody(result);
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("resourceId"));
    }

    @Test
    void verifyEmpty() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var result = performUmaRequest(HttpMethod.DELETE,
            OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/-1",
            results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyBadClientId() throws Throwable {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();
        var result = performUmaRequest(HttpMethod.POST, OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL,
            body, results.getLeft(), results.getMiddle());
        var model = getMappedResponseBody(result);
        val resourceId = ((Number) model.get("resourceId")).longValue();

        val resourceSet = umaResourceSetRepository.getById(resourceId).orElseThrow();
        resourceSet.setOwner("testuser");
        umaResourceSetRepository.save(resourceSet);

        val exception = assertThrows(RuntimeException.class,
            () -> performUmaRequest(HttpMethod.DELETE,
                OAuth20Constants.UMA_RESOURCE_SET_REGISTRATION_URL + "/" + resourceId,
                results.getLeft(), results.getMiddle()));
        assertTrue(exception.getMessage().contains("ClassCastException"));
    }
}
