package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPWorkdayConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.workday.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.workday.name-id-attribute=cn",
    "cas.saml-sp.workday.name-id-format=transient"
})
public class CasSamlSPWorkdayConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
