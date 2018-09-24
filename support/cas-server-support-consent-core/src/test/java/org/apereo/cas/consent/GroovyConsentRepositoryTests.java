package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasConsentCoreConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link GroovyConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasConsentCoreConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreAuditConfiguration.class
})
@TestPropertySource(properties = {
    "cas.consent.groovy.location=classpath:/ConsentRepository.groovy"
})
public class GroovyConsentRepositoryTests extends BaseConsentRepositoryTests {
}
