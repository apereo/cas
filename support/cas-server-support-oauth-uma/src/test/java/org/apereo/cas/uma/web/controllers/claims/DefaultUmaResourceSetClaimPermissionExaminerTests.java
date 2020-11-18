package org.apereo.cas.uma.web.controllers.claims;

import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultUmaResourceSetClaimPermissionExaminerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
public class DefaultUmaResourceSetClaimPermissionExaminerTests extends BaseUmaEndpointControllerTests {

    @Test
    public void verifyUnmatchedOperation() {
        val ticketId = UUID.randomUUID().toString();
        val permissionTicket = mock(UmaPermissionTicket.class);
        when(permissionTicket.getId()).thenReturn(ticketId);
        when(permissionTicket.isExpired()).thenReturn(Boolean.FALSE);
        when(permissionTicket.getClaims()).thenReturn(Map.of("c1", "v1", "c2", "v2"));
        when(permissionTicket.getScopes()).thenReturn(Set.of("s1", "s2", "s3"));

        val id = UUID.randomUUID().toString();

        val resourceSet = new ResourceSet();
        resourceSet.setClientId(id);
        resourceSet.setScopes(CollectionUtils.wrapHashSet("s2"));
        val policy = new ResourceSetPolicy();
        val permission = new ResourceSetPolicyPermission();
        permission.setId(1000);
        permission.setSubject("casuser");
        permission.setClaims(new LinkedHashMap<>(Map.of("c10", "v10")));
        permission.setScopes(CollectionUtils.wrapHashSet("s1", "s2"));
        policy.setPermissions(CollectionUtils.wrapHashSet(permission));
        resourceSet.setPolicies(CollectionUtils.wrapHashSet(policy));
        
        val result = umaResourceSetClaimPermissionExaminer.examine(resourceSet, permissionTicket);
        assertNotNull(result);
        assertTrue(result.getDetails().containsKey(permission.getId()));
    }

    @Test
    public void verifyMatchedOperation() {
        val ticketId = UUID.randomUUID().toString();
        val permissionTicket = mock(UmaPermissionTicket.class);
        when(permissionTicket.getId()).thenReturn(ticketId);
        when(permissionTicket.isExpired()).thenReturn(Boolean.FALSE);
        when(permissionTicket.getClaims()).thenReturn(Map.of("c1", "v1"));
        when(permissionTicket.getScopes()).thenReturn(Set.of("s1", "s2"));

        val id = UUID.randomUUID().toString();

        val resourceSet = new ResourceSet();
        resourceSet.setClientId(id);

        val policy = new ResourceSetPolicy();
        val permission = new ResourceSetPolicyPermission();
        permission.setId(1000);
        permission.setSubject("casuser");
        permission.setClaims(new LinkedHashMap<>(Map.of("c1", "v1")));
        permission.setScopes(CollectionUtils.wrapHashSet("s1", "s2"));
        policy.setPermissions(CollectionUtils.wrapHashSet(permission));
        resourceSet.setPolicies(CollectionUtils.wrapHashSet(policy));

        val result = umaResourceSetClaimPermissionExaminer.examine(resourceSet, permissionTicket);
        assertNotNull(result);
        assertFalse(result.getDetails().containsKey(permission.getId()));
    }


}
