package org.apereo.cas.uma.ticket.resource.repository;

import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasOAuthUmaJpaAutoConfiguration;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicy;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import java.util.LinkedHashMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PostgresJpaResourceSetRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
@ImportAutoConfiguration({
    CasOAuthUmaJpaAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.oauth.uma.resource-set.jpa.user=postgres",
    "cas.authn.oauth.uma.resource-set.jpa.password=password",
    "cas.authn.oauth.uma.resource-set.jpa.driver-class=org.postgresql.Driver",
    "cas.authn.oauth.uma.resource-set.jpa.url=jdbc:postgresql://localhost:5432/oauth",
    "cas.authn.oauth.uma.resource-set.jpa.dialect=org.hibernate.dialect.PostgreSQLDialect",
    
    "cas.jdbc.show-sql=true",
    "cas.authn.oauth.uma.resource-set.jpa.ddl-auto=create-drop"
})
class PostgresJpaResourceSetRepositoryTests extends BaseUmaEndpointControllerTests {

    @Test
    void verifyOperation() {
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
