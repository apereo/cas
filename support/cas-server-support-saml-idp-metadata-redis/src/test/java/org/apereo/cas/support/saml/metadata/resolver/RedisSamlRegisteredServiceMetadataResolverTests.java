package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.support.saml.BaseRedisSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.redis.host=localhost",
    "cas.authn.saml-idp.metadata.redis.port=6379",
    "cas.authn.saml-idp.metadata.file-system.location=file:/tmp"
})
@Tag("Redis")
@EnabledIfPortOpen(port = 6379)
public class RedisSamlRegisteredServiceMetadataResolverTests extends BaseRedisSamlMetadataTests {
    @BeforeEach
    public void setup() {
        val key = RedisSamlRegisteredServiceMetadataResolver.CAS_PREFIX + '*';
        val keys = redisSamlRegisteredServiceMetadataResolverTemplate.keys(key);
        if (keys != null) {
            redisSamlRegisteredServiceMetadataResolverTemplate.delete(keys);
        }
    }

    @Test
    public void verifyResolver() throws IOException {
        val res = new ClassPathResource("sp-metadata.xml");
        val md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        resolver.saveOrUpdate(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("redis://");
        assertTrue(resolver.supports(service));
        assertTrue(resolver.isAvailable(service));
        val resolvers = resolver.resolve(service);
        assertEquals(1, resolvers.size());
    }

    @Test
    public void verifyFailsResolver() throws IOException {
        val res = new ByteArrayResource("bad-data".getBytes(StandardCharsets.UTF_8));
        val md = new SamlMetadataDocument();
        md.setName("SP");
        md.setValue(IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8));
        resolver.saveOrUpdate(md);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        val resolvers = resolver.resolve(service);
        assertTrue(resolvers.isEmpty());
    }

    @Test
    public void verifyResolverDoesNotSupport() {
        assertFalse(resolver.supports(null));
    }
}
