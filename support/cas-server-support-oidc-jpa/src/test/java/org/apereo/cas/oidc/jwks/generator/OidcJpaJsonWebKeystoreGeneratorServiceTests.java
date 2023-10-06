package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.OidcJwksJpaConfiguration;
import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJpaJsonWebKeystoreGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("JDBC")
@TestPropertySource(properties = {
    "cas.authn.oidc.jwks.jpa.ddl-auto=create-drop",
    "cas.authn.oidc.jwks.jpa.url=jdbc:hsqldb:mem:cas-hsql-database"
})
@Import({OidcJwksJpaConfiguration.class, CasHibernateJpaConfiguration.class})
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
public class OidcJpaJsonWebKeystoreGeneratorServiceTests extends AbstractOidcTests {
    @Test
    public void verifyOperation() throws Throwable {
        val resource1 = oidcJsonWebKeystoreGeneratorService.generate();
        val jwks1 = IOUtils.toString(resource1.getInputStream(), StandardCharsets.UTF_8);

        val resource2 = oidcJsonWebKeystoreGeneratorService.generate();
        val jwks2 = IOUtils.toString(resource2.getInputStream(), StandardCharsets.UTF_8);

        assertEquals(jwks1, jwks2);

        val set1 = oidcJsonWebKeystoreGeneratorService.store(OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(resource1));
        assertNotNull(set1);

        assertTrue(oidcJsonWebKeystoreGeneratorService.find().isPresent());
    }
}
