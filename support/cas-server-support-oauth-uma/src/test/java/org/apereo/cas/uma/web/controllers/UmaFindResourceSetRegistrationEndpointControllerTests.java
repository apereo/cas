package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * This is {@link UmaFindResourceSetRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaFindResourceSetRegistrationEndpointControllerTests extends BaseUmaEndpointControllerTests {

    @Test
    public void verifyOperation() throws Exception {
        val results = authenticateUmaRequest();
        var map = new LinkedHashMap<String, Object>();
        map.put("name", "my-resource");
        map.put("type", "my-resource-type");
        map.put("uri", "http://rs.example.com/alice/myresource");
        map.put("resource_scopes", CollectionUtils.wrapList("read", "write"));
        umaCreateResourceSetRegistrationEndpointController.registerResourceSet(MAPPER.writeValueAsString(map), results.getLeft(), results.getMiddle());

        val response = umaFindResourceSetRegistrationEndpointController.findResourceSets(results.getLeft(), results.getMiddle());
        assertNotNull(response.getBody());
        val model = (Collection) response.getBody();
        assertTrue(model.size() == 1);
    }
}
