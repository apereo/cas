package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPSaManageConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.sa-manage.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.sa-manage.name-id-attribute=cn",
    "cas.saml-sp.sa-manage.name-id-format=transient"
})
public class CasSamlSPSaManageConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
