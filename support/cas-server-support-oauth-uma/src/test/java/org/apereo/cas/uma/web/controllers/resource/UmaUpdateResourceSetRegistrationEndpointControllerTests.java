package org.apereo.cas.uma.web.controllers.resource;

import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaUpdateResourceSetRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("UMA")
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

    @Test
    public void verifyFailsId() throws Exception {
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest().toJson();

        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        body = createUmaResourceRegistrationRequest(resourceId).toJson();

        response = umaUpdateResourceSetRegistrationEndpointController.updateResourceSet(-1, body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void verifyFailsMissing() throws Exception {
        val id = RandomUtils.nextLong();
        val results = authenticateUmaRequestWithProtectionScope();
        var body = createUmaResourceRegistrationRequest(id).toJson();
        val response = umaUpdateResourceSetRegistrationEndpointController.updateResourceSet(id, body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void verifyNoAuth() throws Exception {
        var body = createUmaResourceRegistrationRequest(1000).toJson();
        val response = umaUpdateResourceSetRegistrationEndpointController.updateResourceSet(1000, body,
            new MockHttpServletRequest(), new MockHttpServletResponse());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
