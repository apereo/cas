package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseConsentActivationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@SpringBootTest(classes = BaseConsentRepositoryTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseConsentActivationStrategyTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(ConsentActivationStrategy.BEAN_NAME)
    private ConsentActivationStrategy consentActivationStrategy;

    @BeforeEach
    void beforeEach() {
        servicesManager.deleteAll();
    }

    @Test
    void verifyConsentActive() throws Throwable {
        val registeredService = getRegisteredServiceWithConsentStatus(TriStateBoolean.TRUE);
        val service = CoreAuthenticationTestUtils.getWebApplicationService(registeredService.getServiceId());
        assertTrue(getConsentActivationStrategy().isConsentRequired(service, registeredService,
            CoreAuthenticationTestUtils.getAuthentication(), new MockHttpServletRequest()));
    }

    @Test
    void verifyConsentDisabled() throws Throwable {
        val registeredService = getRegisteredServiceWithConsentStatus(TriStateBoolean.FALSE);
        val service = CoreAuthenticationTestUtils.getWebApplicationService(registeredService.getServiceId());
        assertFalse(getConsentActivationStrategy().isConsentRequired(service, registeredService,
            CoreAuthenticationTestUtils.getAuthentication(), new MockHttpServletRequest()));
    }

    @Test
    void verifyConsentUndefined() throws Throwable {
        val registeredService = getRegisteredServiceWithConsentStatus(TriStateBoolean.UNDEFINED);
        val service = CoreAuthenticationTestUtils.getWebApplicationService(registeredService.getServiceId());
        assertTrue(getConsentActivationStrategy().isConsentRequired(service, registeredService,
            CoreAuthenticationTestUtils.getAuthentication(), new MockHttpServletRequest()));
    }

    private RegisteredService getRegisteredServiceWithConsentStatus(final TriStateBoolean status) {
        val id = UUID.randomUUID().toString();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
        val attrPolicy = new ReturnAllAttributeReleasePolicy();
        attrPolicy.setConsentPolicy(new DefaultRegisteredServiceConsentPolicy().setStatus(status));
        registeredService.setAttributeReleasePolicy(attrPolicy);
        servicesManager.save(registeredService);
        return registeredService;
    }
}
