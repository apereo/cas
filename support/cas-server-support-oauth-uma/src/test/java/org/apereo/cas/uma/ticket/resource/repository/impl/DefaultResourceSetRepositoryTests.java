package org.apereo.cas.uma.ticket.resource.repository.impl;

import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultResourceSetRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
public class DefaultResourceSetRepositoryTests {
    @Test
    public void verifyOwner() {
        val repo = new DefaultResourceSetRepository();
        val set = buildTestResource();
        repo.save(set);
        assertFalse(repo.getByOwner("cas").isEmpty());
        assertEquals(1, repo.count());
    }

    @Test
    public void verifyUpdateFails() {
        val repo = new DefaultResourceSetRepository();
        val set1 = buildTestResource();
        val set2 = buildTestResource();
        set2.setId(0);
        assertThrows(IllegalArgumentException.class, () -> repo.update(set1, set2));

        set2.setId(1230);
        assertThrows(IllegalArgumentException.class, () -> repo.update(set1, set2));

        set1.setId(9876);
        set2.setId(set1.getId());
        val perm = new ResourceSetPolicyPermission().setScopes(CollectionUtils.wrapHashSet("unknown"));
        val policy = new ResourceSetPolicy().setPermissions(CollectionUtils.wrapHashSet(perm));
        set2.getPolicies().add(policy);
        assertThrows(IllegalArgumentException.class, () -> repo.update(set1, set2));
    }

    private static ResourceSet buildTestResource() {
        val r = new ResourceSet();
        r.setClientId("clientid");
        r.setIconUri("https://www.example.com/icon");
        r.setName("resource");
        r.setOwner("cas");
        r.setScopes(CollectionUtils.wrapHashSet("read", "write"));
        r.setType("CAS-UMA");
        r.setUri("https://www.example.com/cas");
        return r;
    }
}
