package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPJiraConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.jira.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.jira.name-id-attribute=cn",
    "cas.saml-sp.jira.name-id-format=transient"
})
public class CasSamlSPJiraConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
