package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RankedMultifactorAuthenticationProviderSelectorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Webflow")
public class RankedMultifactorAuthenticationProviderSelectorTests extends BaseCasWebflowMultifactorAuthenticationTests {

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    @Test
    public void verifySelectionOfMfaProvider() {
        val dummy1 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        dummy1.setOrder(10);
        dummy1.setFailureMode(RegisteredServiceMultifactorPolicyFailureModes.PHANTOM.name());
        val dummy2 = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        dummy2.setOrder(5);

        val service = RegisteredServiceTestUtils.getRegisteredService();
        servicesManager.save(service);
        val provider = multifactorAuthenticationProviderSelector.resolve(CollectionUtils.wrapList(dummy1, dummy2),
            service, RegisteredServiceTestUtils.getPrincipal());
        assertNotNull(provider);
        assertEquals(dummy1.getId(), provider.getId());
        assertEquals(dummy1.getOrder(), provider.getOrder());
        assertEquals(RegisteredServiceMultifactorPolicyFailureModes.PHANTOM, provider.getFailureMode());
    }

}
