package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPDropboxConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.dropbox.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.dropbox.name-id-attribute=cn",
    "cas.saml-sp.dropbox.name-id-format=transient"
})
public class CasSamlSPDropboxConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
