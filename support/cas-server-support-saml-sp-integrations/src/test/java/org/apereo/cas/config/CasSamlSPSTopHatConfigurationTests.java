package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPSTopHatConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.top-hat.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.top-hat.name-id-attribute=cn",
    "cas.saml-sp.top-hat.name-id-format=transient"
})
public class CasSamlSPSTopHatConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
