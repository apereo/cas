package org.apereo.cas.util.crypto;

import lombok.val;
import org.jose4j.keys.RsaKeyUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PublicKeyFactoryBeanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class PublicKeyFactoryBeanTests {

    @Test
    public void verifyOperation() throws Exception {
        val factory = new PublicKeyFactoryBean(new ClassPathResource("publickey.pem"), RsaKeyUtil.RSA);
        factory.setSingleton(false);
        val object = factory.getObject();
        assertNotNull(object);
    }
}
