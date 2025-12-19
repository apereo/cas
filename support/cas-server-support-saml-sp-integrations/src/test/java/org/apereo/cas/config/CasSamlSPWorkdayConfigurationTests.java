package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPWorkdayConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.workday.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.workday.name-id-attribute=cn",
    "cas.saml-sp.workday.name-id-format=transient"
})
class CasSamlSPWorkdayConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
