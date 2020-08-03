package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPRocketChatConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.rocket-chat.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.rocket-chat.name-id-attribute=cn",
    "cas.saml-sp.rocket-chat.name-id-format=transient"
})
public class CasSamlSPRocketChatConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
