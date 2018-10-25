package org.apereo.cas.uma.web.controllers.resource;

import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaDeleteResourceSetRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaDeleteResourceSetRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {

    @Test
    public void verifyOperation() throws Exception {
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
}
