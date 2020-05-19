package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPQualtricsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.qualtrics.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.qualtrics.name-id-attribute=cn",
    "cas.saml-sp.qualtrics.name-id-format=transient"
})
public class CasSamlSPQualtricsConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
