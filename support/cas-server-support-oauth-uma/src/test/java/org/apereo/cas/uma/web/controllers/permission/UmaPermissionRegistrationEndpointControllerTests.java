package org.apereo.cas.uma.web.controllers.permission;

import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link UmaPermissionRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */

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
}
