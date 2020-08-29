package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.support.saml.BaseJpaSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("JDBC")
public class JpaSamlRegisteredServiceMetadataResolverTests extends BaseJpaSamlMetadataTests {

    @PersistenceContext(unitName = "samlMetadataEntityManagerFactory")
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        entityManager.createQuery("DELETE FROM SamlMetadataDocument");
    }

    @Test
    public void verifyResolver() throws Exception {
        val res = new ClassPathResource("samlsp-metadata.xml");
        val md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        resolver.saveOrUpdate(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("jdbc://");
        assertTrue(resolver.supports(service));
        assertFalse(resolver.supports(null));
        assertTrue(resolver.isAvailable(service));
        val resolvers = resolver.resolve(service);
        assertEquals(1, resolvers.size());
    }
}
