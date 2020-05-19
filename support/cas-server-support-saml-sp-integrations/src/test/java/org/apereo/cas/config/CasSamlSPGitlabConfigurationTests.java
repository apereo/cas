package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPGitlabConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.gitlab.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.gitlab.name-id-attribute=cn",
    "cas.saml-sp.gitlab.name-id-format=transient"
})
public class CasSamlSPGitlabConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
