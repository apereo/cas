package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link InMemoryConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class)
@Getter
@Tag("Consent")
@ExtendWith(CasTestExtension.class)
class InMemoryConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier(ConsentRepository.BEAN_NAME)
    protected ConsentRepository repository;
}
