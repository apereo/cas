package org.apereo.cas.support.saml.util.credential;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.security.credential.BasicCredential;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BasicResourceCredentialFactoryBeanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class BasicResourceCredentialFactoryBeanTests {
    @Test
    public void verifyKeys() throws Exception {
        val factory = new BasicResourceCredentialFactoryBean();
        assertEquals(BasicCredential.class, factory.getObjectType());
        assertTrue(factory.isSingleton());
        factory.setUsageType("UNSPECIFIED");
        factory.setPrivateKeyInfo(new ClassPathResource("keys/private.pem"));
        factory.setPublicKeyInfo(new ClassPathResource("keys/public.pem"));
        assertNotNull(factory.getObject());
    }

    @Test
    public void verifyMissingPrivKeys() throws Exception {
        val factory = new BasicResourceCredentialFactoryBean();
        factory.setPrivateKeyInfo(new ClassPathResource("keys/badprivate.pem"));
        assertThrows(BeanCreationException.class, factory::getObject);
    }

    @Test
    public void verifyMissingSecretKeys() throws Exception {
        val factory = new BasicResourceCredentialFactoryBean();
        factory.setSecretKeyInfo(new ClassPathResource("keys/badsec.pem"));
        assertThrows(BeanCreationException.class, factory::getObject);
    }

    @Test
    public void verifyNoKeys() throws Exception {
        val factory = new BasicResourceCredentialFactoryBean();
        assertThrows(BeanCreationException.class, factory::getObject);
    }

    @Test
    public void verifyMismatchedKeys() throws Exception {
        val factory = new BasicResourceCredentialFactoryBean();
        factory.setPrivateKeyInfo(new ClassPathResource("keys/private.pem"));
        factory.setPublicKeyInfo(new ClassPathResource("keys/badpublic.key"));
        assertThrows(BeanCreationException.class, factory::getObject);
    }

    @Test
    public void verifyPublicKeys() throws Exception {
        val factory = new BasicResourceCredentialFactoryBean();
        assertEquals(BasicCredential.class, factory.getObjectType());
        assertTrue(factory.isSingleton());
        factory.setUsageType("UNSPECIFIED");
        factory.setPublicKeyInfo(new ClassPathResource("keys/public.pem"));
        assertNotNull(factory.getObject());
    }

    @Test
    public void verifySecretKeys() throws Exception {
        val factory = new BasicResourceCredentialFactoryBean();
        factory.setUsageType("UNSPECIFIED");
        factory.setSecretKeyInfo(new ClassPathResource("keys/secret.key"));
        factory.setSecretKeyAlgorithm("RSA");

        factory.setSecretKeyEncoding(BasicResourceCredentialFactoryBean.SecretKeyEncoding.BINARY);
        assertNotNull(factory.getObject());

        factory.setSecretKeyEncoding(BasicResourceCredentialFactoryBean.SecretKeyEncoding.HEX);
        assertThrows(BeanCreationException.class, factory::getObject);

        factory.setSecretKeyEncoding(BasicResourceCredentialFactoryBean.SecretKeyEncoding.BASE64);
        assertNotNull(factory.getObject());
    }
}
