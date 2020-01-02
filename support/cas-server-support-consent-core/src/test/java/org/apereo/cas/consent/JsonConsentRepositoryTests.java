package org.apereo.cas.consent;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link JsonConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class,
    properties = "cas.consent.json.location=classpath:/ConsentRepository.json")
@Getter
@Tag("FileSystem")
public class JsonConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;
}
