package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPSlackConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.slack.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.slack.name-id-attribute=cn",
    "cas.saml-sp.slack.name-id-format=transient"
})
public class CasSamlSPSlackConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
