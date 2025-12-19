package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPJiraConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.jira.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.jira.name-id-attribute=cn",
    "cas.saml-sp.jira.name-id-format=transient"
})
class CasSamlSPJiraConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
