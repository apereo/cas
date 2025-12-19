package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultConsentActivationStrategyDisabledTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Consent")
@ExtendWith(CasTestExtension.class)
@Getter
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class,
    properties = "cas.consent.core.active=false")
class DefaultConsentActivationStrategyDisabledTests {

    @Autowired
    @Qualifier(ConsentActivationStrategy.BEAN_NAME)
    private ConsentActivationStrategy consentActivationStrategy;
    
    @Test
    void verifyNoConsent() throws Throwable {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(mock(RegisteredServiceAttributeReleasePolicy.class));
        assertFalse(getConsentActivationStrategy().isConsentRequired(
            CoreAuthenticationTestUtils.getService(),
            registeredService,
            CoreAuthenticationTestUtils.getAuthentication(),
            new MockHttpServletRequest()));
    }
}
