package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
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
        val results = authenticateUmaRequest();

        var map = new LinkedHashMap<String, Object>();
        map.put("name", "my-resource");
        map.put("resource_scopes", CollectionUtils.wrapList("read", "write"));
        var body = MAPPER.writeValueAsString(map);

        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(body, results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        map = new LinkedHashMap<>();
        map.put("name", "new-resource");
        map.put("_id", resourceId);
        map.put("resource_scopes", CollectionUtils.wrapList("read"));
        body = MAPPER.writeValueAsString(map);
        
        response = umaUpdateResourceSetRegistrationEndpointController.updateResourceSet(resourceId, body, results.getLeft(), results.getMiddle());
        assertNotNull(response.getBody());
        model = (Map) response.getBody();
        assertTrue(model.containsKey("entity"));
        assertTrue(model.containsKey("location"));
        assertTrue(model.containsKey("resourceId"));
    }
}
