package org.apereo.cas.support.saml;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class SamlUtilsTests {

    @Test
    public void verifyOperation() {
        val x509 = SamlUtils.readCertificate(new ClassPathResource("idp-signing.crt"));
        assertNotNull(x509);
    }
}
