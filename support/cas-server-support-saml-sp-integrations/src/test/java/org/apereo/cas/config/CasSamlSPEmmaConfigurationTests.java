package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPEmmaConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.emma.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.emma.name-id-attribute=cn",
    "cas.saml-sp.emma.name-id-format=transient"
})
public class CasSamlSPEmmaConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
