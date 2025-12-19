package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPPollEverywhereConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.poll-everywhere.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.poll-everywhere.name-id-attribute=cn",
    "cas.saml-sp.poll-everywhere.name-id-format=transient"
})
class CasSamlSPPollEverywhereConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
