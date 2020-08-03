package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPAcademicHealthPlansConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.academic-health-plans.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.academic-health-plans.name-id-attribute=cn",
    "cas.saml-sp.academic-health-plans.name-id-format=transient"
})
public class CasSamlSPAcademicHealthPlansConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
