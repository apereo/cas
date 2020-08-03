package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPDocuSignConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.docu-sign.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.docu-sign.name-id-attribute=cn",
    "cas.saml-sp.docu-sign.name-id-format=transient"
})
public class CasSamlSPDocuSignConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
