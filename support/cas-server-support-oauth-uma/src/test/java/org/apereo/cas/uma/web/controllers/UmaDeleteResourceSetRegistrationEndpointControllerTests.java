package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link UmaDeleteResourceSetRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaDeleteResourceSetRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {

    @Test
    public void verifyOperation() throws Exception {
        val results = authenticateUmaRequest();
        var map = new LinkedHashMap<String, Object>();
        map.put("name", "resource");
        map.put("type", "my-resource-type");
        map.put("uri", "http://rs.example.com/alice/myresource");
        map.put("resource_scopes", CollectionUtils.wrapList("read", "write"));
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(MAPPER.writeValueAsString(map), results.getLeft(), results.getMiddle());
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
