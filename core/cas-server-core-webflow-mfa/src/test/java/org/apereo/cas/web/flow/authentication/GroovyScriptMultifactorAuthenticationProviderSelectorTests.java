package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationProviderSelectorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Groovy")
@TestPropertySource(properties = "cas.authn.mfa.providerSelectorGroovyScript=classpath:mfaGroovySelector.groovy")
public class GroovyScriptMultifactorAuthenticationProviderSelectorTests extends BaseCasWebflowMultifactorAuthenticationTests {

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    @Test
    public void verifySelectionOfMfaProvider() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        servicesManager.save(service);
        val dummy = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = multifactorAuthenticationProviderSelector.resolve(CollectionUtils.wrapList(dummy),
            service, RegisteredServiceTestUtils.getPrincipal());
        assertNotNull(provider);
        assertEquals(TestMultifactorAuthenticationProvider.ID, provider.getId());
    }

}
