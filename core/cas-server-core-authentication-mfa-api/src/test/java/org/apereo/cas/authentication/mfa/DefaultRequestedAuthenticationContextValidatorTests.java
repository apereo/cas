package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.validation.Assertion;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorAuthenticationContextValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.5
 */
@Tag("MFA")
public class DefaultRequestedAuthenticationContextValidatorTests {

    private static final String CASUSER = "casuser";

    private static final Map<String, List<Object>> AUTH_ATTRIBUTES = CollectionUtils.wrap("givenName", "CAS");

    private static ConfigurableApplicationContext buildApplicationContext() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());
        return applicationContext;
    }

    @Test
    public void verifyNoRequestedAuthenticationContext() {
        val applicationContext = buildApplicationContext();

        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.empty(), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(CASUSER);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.isSuccess());
    }

    @Test
    public void verifyExecutionIgnoredPerService() {
        val applicationContext = buildApplicationContext();

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val props = MultifactorAuthenticationTestUtils.getAuthenticationBypassProperties();
        val bypass = new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
        provider.setBypassEvaluator(bypass);
        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.of(provider), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED.toString());

        val mfaPolicy = mock(RegisteredServiceMultifactorPolicy.class);
        when(mfaPolicy.isBypassEnabled()).thenReturn(true);
        val service = MultifactorAuthenticationTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = MultifactorAuthenticationTestUtils.getRegisteredService(service.getId(),
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED.toString());
        when(registeredService.getMultifactorPolicy()).thenReturn(mfaPolicy);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val assertion = mock(Assertion.class);
        when(assertion.getService()).thenReturn(service);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, CollectionUtils.wrap(CASUSER, AUTH_ATTRIBUTES));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.isSuccess());
        assertTrue(result.getContextId().isEmpty());
    }

    @Test
    public void verifyRequestedAuthenticationContextChained() {
        val applicationContext = buildApplicationContext();

        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().getCore()
            .setGlobalFailureMode(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.OPEN);
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        val chainProvider = new DefaultChainingMultifactorAuthenticationProvider(failureEvaluator);

        val provider1 = new TestMultifactorAuthenticationProvider("mfa-first");
        val provider2 = new TestMultifactorAuthenticationProvider("mfa-second");
        chainProvider.addMultifactorAuthenticationProvider(provider1);
        chainProvider.addMultifactorAuthenticationProvider(provider2);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, provider1);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, provider2);

        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.of(chainProvider), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED.toString());
        
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, CollectionUtils.wrap(CASUSER, AUTH_ATTRIBUTES));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        auth.getAttributes().put("authn_method", List.of(provider2.getId()));
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.isSuccess());
    }

    @Test
    public void verifyRequestedAuthenticationContextBypassed() {
        val applicationContext = buildApplicationContext();

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val props = MultifactorAuthenticationTestUtils.getAuthenticationBypassProperties();
        val bypass = new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
        provider.setBypassEvaluator(bypass);
        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.of(provider), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER, CollectionUtils.wrap(CASUSER, AUTH_ATTRIBUTES));
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.isSuccess());
    }

    @Test
    public void verifyRequestedAuthenticationContextNotBypassed() {
        val applicationContext = buildApplicationContext();
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationAttributeName("givenName");
        props.setAuthenticationAttributeValue("Not Bypassed");
        val bypass = new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, TestMultifactorAuthenticationProvider.ID);
        provider.setBypassEvaluator(bypass);
        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.of(provider), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertFalse(result.isSuccess());
    }

    @Test
    public void verifyRequestedAuthenticationIsAlreadyBypass() {
        val applicationContext = buildApplicationContext();

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationAttributeName("givenName");
        props.setAuthenticationAttributeValue("Not Bypassed");
        val bypass = new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, TestMultifactorAuthenticationProvider.ID);
        provider.setBypassEvaluator(bypass);
        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.of(provider), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED.toString());
        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);

        val attrs = new HashMap<String, List<Object>>();
        attrs.put(MultifactorAuthenticationProviderBypassEvaluator.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, List.of(true));
        attrs.put(MultifactorAuthenticationProviderBypassEvaluator.AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER,
            List.of(TestMultifactorAuthenticationProvider.ID));

        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, attrs);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.isSuccess());
    }

    @Test
    public void verifyRequestedAuthenticationContextNoProvider() {
        val applicationContext = buildApplicationContext();

        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.of(new TestMultifactorAuthenticationProvider()), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED.toString());

        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertFalse(result.isSuccess());
    }

    @Test
    public void verifyGlobalFailureModeFailsOpen() {
        val applicationContext = buildApplicationContext();

        val provider = TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().getCore().setGlobalFailureMode(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.OPEN);
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        provider.setFailureModeEvaluator(failureEvaluator);

        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.of(provider), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED.toString());

        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.isSuccess());
    }

    @Test
    public void verifyGlobalFailureModeFailsClosed() {
        val applicationContext = buildApplicationContext();

        TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(TestUnavailableMultifactorAuthenticationProvider.ID,
            applicationContext);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().getCore()
            .setGlobalFailureMode(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.CLOSED);
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        ((TestUnavailableMultifactorAuthenticationProvider) provider.get()).setFailureModeEvaluator(failureEvaluator);

        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.of(new TestMultifactorAuthenticationProvider()), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED.toString());

        val assertion = mock(Assertion.class);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertFalse(result.isSuccess());
    }

    @Test
    public void verifyServiceFailureModeFailsOpen() {
        val applicationContext = buildApplicationContext();

        val provider = TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().getCore()
            .setGlobalFailureMode(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.CLOSED);
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        provider.setFailureModeEvaluator(failureEvaluator);

        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.of(provider), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.OPEN.toString());
        val assertion = mock(Assertion.class);
        val service = MultifactorAuthenticationTestUtils.getService("service");
        when(assertion.getService()).thenReturn(service);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertTrue(result.isSuccess());
    }

    @Test
    public void verifyServiceFailureModeFailsClosed() {
        val applicationContext = buildApplicationContext();

        val provider = TestUnavailableMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().getCore().setGlobalFailureMode(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.OPEN);
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        provider.setFailureModeEvaluator(failureEvaluator);

        val servicesManager = mock(ServicesManager.class);
        val validator = MultifactorAuthenticationTestUtils.mockRequestAuthnContextValidator(servicesManager,
            Optional.of(provider), applicationContext,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.CLOSED.toString());
        val assertion = mock(Assertion.class);
        val service = MultifactorAuthenticationTestUtils.getService("service");
        when(assertion.getService()).thenReturn(service);
        val principal = MultifactorAuthenticationTestUtils.getPrincipal(CASUSER);
        val auth = MultifactorAuthenticationTestUtils.getAuthentication(principal, AUTH_ATTRIBUTES);
        when(assertion.getPrimaryAuthentication()).thenReturn(auth);
        val result = validator.validateAuthenticationContext(assertion, new MockHttpServletRequest());
        assertFalse(result.isSuccess());
    }
}
