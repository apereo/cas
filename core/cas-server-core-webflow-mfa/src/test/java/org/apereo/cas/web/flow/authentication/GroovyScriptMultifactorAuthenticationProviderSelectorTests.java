package org.apereo.cas.web.flow.authentication;

import module java.base;
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
@Tag("GroovyAuthentication")
@TestPropertySource(properties = "cas.authn.mfa.core.provider-selection.provider-selector-groovy-script.location=classpath:mfaGroovySelector.groovy")
class GroovyScriptMultifactorAuthenticationProviderSelectorTests extends BaseCasWebflowMultifactorAuthenticationTests {

    @Autowired
    @Qualifier(MultifactorAuthenticationProviderSelector.BEAN_NAME)
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    @Test
    void verifySelectionOfMfaProvider() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        servicesManager.save(service);
        val dummy = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = multifactorAuthenticationProviderSelector.resolve(CollectionUtils.wrapList(dummy),
            service, RegisteredServiceTestUtils.getPrincipal());
        assertNotNull(provider);
        assertEquals(TestMultifactorAuthenticationProvider.ID, provider.getId());
    }

    @Test
    void verifyNoProvider() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        servicesManager.save(service);
        val dummy = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = multifactorAuthenticationProviderSelector.resolve(CollectionUtils.wrapList(dummy),
            service, RegisteredServiceTestUtils.getPrincipal("none"));
        assertNull(provider);
    }

}
