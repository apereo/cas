package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
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
        val results = authenticateUmaRequest();
        val map = new LinkedHashMap<String, Object>();
        map.put("resource_set_id", 1234567890);
        map.put("resource_scopes", CollectionUtils.wrapList("read"));
        val response = umaPermissionRegistrationEndpointController.handle(MAPPER.writeValueAsString(map), results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        val model = (Map) response.getBody();
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("message"));
    }
}
