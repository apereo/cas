package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link UmaCreateResourceSetRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaCreateResourceSetRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {

    @Test
    public void verifyRegistrationOperation() throws Exception {
        val results = authenticateUmaRequest();
        var map = new LinkedHashMap<String, Object>();
        map.put("name", "my-resource");
        map.put("type", "my-resource-type");
        map.put("uri", "http://rs.example.com/alice/myresource");
        map.put("resource_scopes", CollectionUtils.wrapList("read", "write"));
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(MAPPER.writeValueAsString(map), results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        var model = (Map) response.getBody();
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("location"));
        assertTrue(model.containsKey("entity"));
        assertTrue(model.containsKey("resourceId"));

        val resourceId = (long) model.get("resourceId");
        response = umaFindResourceSetRegistrationEndpointController.findResourceSet(resourceId, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        model = (Map) response.getBody();
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));

        map = new LinkedHashMap<>();
        map.put("resource_set_id", resourceId);
        map.put("resource_scopes", CollectionUtils.wrapList("read"));
        response = umaPermissionRegistrationEndpointController.handle(MAPPER.writeValueAsString(map), results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        model = (Map) response.getBody();
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));

    }
}
