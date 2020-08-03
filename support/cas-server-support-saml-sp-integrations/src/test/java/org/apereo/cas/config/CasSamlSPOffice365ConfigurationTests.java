package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPOffice365ConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.office365.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.office365.name-id-attribute=cn",
    "cas.saml-sp.office365.name-id-format=transient"
})
public class CasSamlSPOffice365ConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
