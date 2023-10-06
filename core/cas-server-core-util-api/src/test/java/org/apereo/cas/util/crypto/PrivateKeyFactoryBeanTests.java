package org.apereo.cas.util.crypto;

import lombok.val;
import org.jose4j.keys.RsaKeyUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrivateKeyFactoryBeanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("X509")
class PrivateKeyFactoryBeanTests {

    @Test
    void verifyOperation() throws Throwable {
        val factory = new PrivateKeyFactoryBean();
        factory.setLocation(new ClassPathResource("privatekey2.pem"));
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setSingleton(false);
        val object = factory.getObject();
        assertNotNull(object);
    }

    @Test
    void verifyFails() throws Throwable {
        val factory = new PrivateKeyFactoryBean();
        factory.setLocation(new ClassPathResource("badkey.pem"));
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setSingleton(false);
        assertNull(factory.getObject());
    }
}
