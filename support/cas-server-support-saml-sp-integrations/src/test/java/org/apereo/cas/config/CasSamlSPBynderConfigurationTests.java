package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPBynderConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.bynder.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.bynder.name-id-attribute=cn",
    "cas.saml-sp.bynder.name-id-format=transient"
})
public class CasSamlSPBynderConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
