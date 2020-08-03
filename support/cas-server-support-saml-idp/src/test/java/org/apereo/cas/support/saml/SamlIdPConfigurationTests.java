package org.apereo.cas.support.saml;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("SAML")
public class SamlIdPConfigurationTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifySigValidationFilterByRes() throws Exception {
        val filter = SamlUtils.buildSignatureValidationFilter(new ClassPathResource("metadata/idp-signing.crt"));
        assertNotNull(filter);
    }

    @Test
    public void verifySigValidationFilterPublicKey() throws Exception {
        val filter = SamlUtils.buildSignatureValidationFilter(new ClassPathResource("public-key.pem"));
        assertNotNull(filter);
    }

    @Test
    public void verifySigValidationFilter() {
        val filter = SamlUtils.buildSignatureValidationFilter(applicationContext, "classpath:metadata/idp-signing.crt");
        assertNotNull(filter);
    }

    @Test
    public void verifySigValidationFilterByPath() throws Exception {
        val filter = SamlUtils.buildSignatureValidationFilter("classpath:metadata/idp-signing.crt");
        assertNotNull(filter);
    }
}
