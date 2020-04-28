package org.apereo.cas.support.saml.util.credential;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BasicX509CredentialFactoryBeanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class BasicX509CredentialFactoryBeanTests {

    @Test
    public void verifyNoCert() {
        val factory = new BasicX509CredentialFactoryBean();
        assertEquals(BasicX509Credential.class, factory.getObjectType());
        assertTrue(factory.isSingleton());
        assertThrows(BeanCreationException.class, factory::getObject);
    }

    @Test
    public void verifyCred() throws Exception {
        val factory = new BasicX509CredentialFactoryBean();
        factory.setEntityID("example.entityid");
        factory.setKeyNames(List.of("cas"));
        factory.setUsageType("UNSPECIFIED");
        factory.setPrivateKeyResource(new ClassPathResource("keys/private.pem"));
        factory.setCertificateResources(List.of(new ClassPathResource("keys/cert.pem")));
        assertNotNull(factory.getObject());
    }

    @Test
    public void verifyBadEntityKeys() {
        val factory = new BasicX509CredentialFactoryBean();
        factory.setEntityResource(new ClassPathResource("keys/xyz.pem"));
        factory.setCertificateResources(List.of(new ClassPathResource("keys/cert.pem")));
        assertThrows(BeanCreationException.class, factory::getObject);
    }

    @Test
    public void verifyBadPrivateKeys() {
        val factory = new BasicX509CredentialFactoryBean();
        factory.setPrivateKeyResource(new ClassPathResource("keys/xyz.pem"));
        factory.setCertificateResources(List.of(new ClassPathResource("keys/cert.pem")));
        assertThrows(BeanCreationException.class, factory::getObject);
    }

    @Test
    public void verifyCredEntityWithBadCrl() throws Exception {
        val factory = new BasicX509CredentialFactoryBean();
        factory.setEntityID("example.entityid");
        factory.setKeyNames(List.of("cas"));
        factory.setUsageType("UNSPECIFIED");
        factory.setPrivateKeyResource(new ClassPathResource("keys/private.pem"));
        factory.setCertificateResources(List.of(new ClassPathResource("keys/cert.pem")));
        factory.setEntityResource(new ClassPathResource("keys/cert.pem"));
        factory.setCrlResources(List.of(new ClassPathResource("keys/cert.pem")));
        assertThrows(BeanCreationException.class, factory::getObject);
    }
}
