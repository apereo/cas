package org.apereo.cas.uma.web.controllers;

import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.Pac4jUtils;

import lombok.val;
import org.junit.Test;
import org.pac4j.core.profile.CommonProfile;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link UmaCreatePolicyForResourceSetEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaCreatePolicyForResourceSetEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyOperation() throws Exception {
        val results = authenticateUmaRequest();
        var map = new LinkedHashMap<String, Object>();
        map.put("name", "my-resource");
        map.put("type", "my-resource-type");
        map.put("uri", "http://rs.example.com/alice/myresource");
        map.put("resource_scopes", CollectionUtils.wrapList("read", "write"));
        var response = umaCreateResourceSetRegistrationEndpointController.registerResourceSet(MAPPER.writeValueAsString(map), results.getLeft(), results.getMiddle());

        var model = (Map) response.getBody();
        val resourceId = (long) model.get("resourceId");

        val policy = new ResourceSetPolicy();
        val perm = new ResourceSetPolicyPermission();
        perm.setScopes(CollectionUtils.wrapHashSet("read", "write"));
        final CommonProfile profile = (CommonProfile) Pac4jUtils.getPac4jProfileManager(results.getLeft(), results.getMiddle()).get(true).get();
        perm.setSubject(profile.getId());
        policy.setPermissions(CollectionUtils.wrapHashSet(perm));
        val body = MAPPER.writeValueAsString(policy);

        response = umaCreatePolicyForResourceSetEndpointController.createPolicyForResourceSet(resourceId, body, results.getLeft(), results.getMiddle());
        model = (Map) response.getBody();
        assertTrue(model.containsKey("code"));
        assertTrue(model.containsKey("entity"));
    }
}
