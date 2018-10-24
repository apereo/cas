package org.apereo.cas.uma.web.controllers.resource;

import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link UmaUpdateResourceSetRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaUpdateResourceSetRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {

    @Test
    public void verifyRegistrationOperation() throws Exception {
        val results = authenticateUmaRequestWithProtectionScope();

        var body = createUmaResourceRegistrationRequest().toJson();

        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        body = createUmaResourceRegistrationRequest(resourceId).toJson();

        response = umaUpdateResourceSetRegistrationEndpointController.updateResourceSet(resourceId, body, results.getLeft(), results.getMiddle());
        assertNotNull(response.getBody());
        model = (Map) response.getBody();
        assertTrue(model.containsKey("entity"));
        assertTrue(model.containsKey("location"));
        assertTrue(model.containsKey("resourceId"));
    }
}
