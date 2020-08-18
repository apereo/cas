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
public class PrivateKeyFactoryBeanTests {

    @Test
    public void verifyOperation() throws Exception {
        val factory = new PrivateKeyFactoryBean();
        factory.setLocation(new ClassPathResource("privatekey2.pem"));
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setSingleton(false);
        val object = factory.getObject();
        assertNotNull(object);
    }

    @Test
    public void verifyFails() throws Exception {
        val factory = new PrivateKeyFactoryBean();
        factory.setLocation(new ClassPathResource("badkey.pem"));
        factory.setAlgorithm(RsaKeyUtil.RSA);
        factory.setSingleton(false);
        assertNull(factory.getObject());
    }
}
