package org.apereo.cas.consent;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link GroovyConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class,
    properties = "cas.consent.groovy.location=classpath:/ConsentRepository.groovy")
@Getter
@Tag("Groovy")
public class GroovyConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;
}
