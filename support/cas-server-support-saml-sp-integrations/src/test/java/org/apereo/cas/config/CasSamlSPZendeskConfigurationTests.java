package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPZendeskConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.zendesk.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.zendesk.name-id-attribute=cn",
    "cas.saml-sp.zendesk.name-id-format=transient"
})
class CasSamlSPZendeskConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
