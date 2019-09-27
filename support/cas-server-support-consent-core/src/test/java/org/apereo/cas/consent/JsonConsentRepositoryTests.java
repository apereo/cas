package org.apereo.cas.consent;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasConsentCoreConfiguration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link JsonConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasConsentCoreConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreAuditConfiguration.class
}, properties = "cas.consent.json.location=classpath:/ConsentRepository.json")
@Getter
public class JsonConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;
}
