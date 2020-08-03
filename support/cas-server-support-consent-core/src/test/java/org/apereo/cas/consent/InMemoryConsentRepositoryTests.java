package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentCoreConfiguration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link InMemoryConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasConsentCoreConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
})
@Getter
@Tag("Simple")
public class InMemoryConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;
}
