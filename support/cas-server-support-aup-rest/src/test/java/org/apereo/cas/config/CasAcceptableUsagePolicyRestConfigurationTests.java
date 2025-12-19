package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.BaseAcceptableUsagePolicyRepositoryTests;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasAcceptableUsagePolicyRestConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasAcceptableUsagePolicyRestAutoConfiguration.class,
    BaseAcceptableUsagePolicyRepositoryTests.SharedTestConfiguration.class
})
class CasAcceptableUsagePolicyRestConfigurationTests {
    @Autowired
    @Qualifier(AcceptableUsagePolicyRepository.BEAN_NAME)
    private AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Test
    void verifyOperation() {
        assertNotNull(acceptableUsagePolicyRepository);
    }
}
