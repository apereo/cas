package org.apereo.cas.support.saml.util.credential;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BasicX509CredentialFactoryBeanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
class BasicX509CredentialFactoryBeanTests {

    @Test
    void verifyNoCert() {
        val factory = new BasicX509CredentialFactoryBean();
        assertSame(BasicX509Credential.class, factory.getObjectType());
        assertTrue(factory.isSingleton());
        assertThrows(BeanCreationException.class, factory::getObject);
    }

    @Test
    void verifyCred() throws Throwable {
        val factory = new BasicX509CredentialFactoryBean();
        factory.setEntityID("example.entityid");
        factory.setKeyNames(List.of("cas"));
        factory.setUsageType("UNSPECIFIED");
        factory.setPrivateKeyResource(new ClassPathResource("keys/private.pem"));
        factory.setCertificateResources(List.of(new ClassPathResource("keys/cert.pem")));
        assertNotNull(factory.getObject());
    }

    @Test
    void verifyBadEntityKeys() {
        val factory = new BasicX509CredentialFactoryBean();
        factory.setEntityResource(new ClassPathResource("keys/xyz.pem"));
        factory.setCertificateResources(List.of(new ClassPathResource("keys/cert.pem")));
        assertThrows(BeanCreationException.class, factory::getObject);
    }

    @Test
    void verifyBadPrivateKeys() {
        val factory = new BasicX509CredentialFactoryBean();
        factory.setPrivateKeyResource(new ClassPathResource("keys/xyz.pem"));
        factory.setCertificateResources(List.of(new ClassPathResource("keys/cert.pem")));
        assertThrows(BeanCreationException.class, factory::getObject);
    }

    @Test
    void verifyCredEntityWithBadCrl() {
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
