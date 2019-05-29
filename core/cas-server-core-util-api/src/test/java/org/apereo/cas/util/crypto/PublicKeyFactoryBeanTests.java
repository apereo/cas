package org.apereo.cas.util.crypto;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PublicKeyFactoryBeanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class PublicKeyFactoryBeanTests {

    @Test
    public void verifyOperation() throws Exception {
        val factory = new PublicKeyFactoryBean();
        factory.setResource(new ClassPathResource("publickey.pem"));
        factory.setSingleton(false);
        factory.setAlgorithm("RSA");
        val object = factory.getObject();
        assertNotNull(object);
    }
}
