package org.apereo.cas.uma.ticket.resource.repository;

import org.apereo.cas.config.CasOAuthUmaJpaConfiguration;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * This is {@link JpaResourceSetRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Import(CasOAuthUmaJpaConfiguration.class)
@TestPropertySource(properties = "cas.authn.uma.resourceSet.jpa.url=jdbc:hsqldb:mem:cas-uma-resourceset")
public class JpaResourceSetRepositoryTests extends BaseUmaEndpointControllerTests {

    @Autowired
    @Qualifier("umaResourceSetRepository")
    protected ResourceSetRepository umaResourceSetRepository;

    @Test
    public void verifyOperation() {
        var r = buildTestResource();
        assertTrue(umaResourceSetRepository.getAll().isEmpty());
        assertFalse(umaResourceSetRepository.getById(r.getId()).isPresent());

        r = umaResourceSetRepository.save(r);
        assertFalse(umaResourceSetRepository.getAll().isEmpty());
        assertTrue(umaResourceSetRepository.getById(r.getId()).isPresent());

        val perms = new ResourceSetPolicyPermission();
        perms.setSubject("casuser");
        perms.setScopes(CollectionUtils.wrapHashSet("read", "write"));
        perms.setClaims(new LinkedHashMap<>(CollectionUtils.wrap("givenName", "CAS")));

        val policy = new ResourceSetPolicy();
        policy.setPermissions(CollectionUtils.wrapHashSet(perms));
        r.setOwner("UMA");
        r.setPolicies(CollectionUtils.wrapHashSet(policy));
        r = umaResourceSetRepository.save(r);
        assertEquals("UMA", r.getOwner());
        assertFalse(r.getPolicies().isEmpty());

        umaResourceSetRepository.removeAll();
        assertTrue(umaResourceSetRepository.getAll().isEmpty());
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
