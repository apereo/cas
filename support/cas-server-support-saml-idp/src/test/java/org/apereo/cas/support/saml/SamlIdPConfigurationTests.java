package org.apereo.cas.support.saml;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("SAML2")
class SamlIdPConfigurationTests extends BaseSamlIdPConfigurationTests {
    @Test
    void verifySigValidationFilterByRes() throws Throwable {
        val filter = SamlUtils.buildSignatureValidationFilter(new ClassPathResource("metadata/idp-signing.crt"));
        assertNotNull(filter);
    }

    @Test
    void verifySigValidationFilterPublicKey() throws Throwable {
        val filter = SamlUtils.buildSignatureValidationFilter(new ClassPathResource("public-key.pem"));
        assertNotNull(filter);
    }

    @Test
    void verifySigValidationFilter() {
        val filter = SamlUtils.buildSignatureValidationFilter(applicationContext, "classpath:metadata/idp-signing.crt");
        assertNotNull(filter);
    }

    @Test
    void verifySigValidationFilterByPath() throws Throwable {
        val filter = SamlUtils.buildSignatureValidationFilter("classpath:metadata/idp-signing.crt");
        assertNotNull(filter);
    }
}
