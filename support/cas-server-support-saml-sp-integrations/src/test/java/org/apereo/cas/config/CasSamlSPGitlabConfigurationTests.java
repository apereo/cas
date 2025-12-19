package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPGitlabConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.gitlab.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.gitlab.name-id-attribute=cn",
    "cas.saml-sp.gitlab.name-id-format=transient"
})
class CasSamlSPGitlabConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
