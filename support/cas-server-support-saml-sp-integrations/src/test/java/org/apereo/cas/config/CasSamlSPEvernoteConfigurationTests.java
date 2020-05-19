package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPEvernoteConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.evernote.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.evernote.name-id-attribute=cn",
    "cas.saml-sp.evernote.name-id-format=transient"
})
public class CasSamlSPEvernoteConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
