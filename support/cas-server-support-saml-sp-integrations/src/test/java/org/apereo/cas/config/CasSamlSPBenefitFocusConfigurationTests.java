package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPBenefitFocusConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.benefit-focus.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.benefit-focus.name-id-attribute=cn",
    "cas.saml-sp.benefit-focus.name-id-format=transient"
})
public class CasSamlSPBenefitFocusConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
