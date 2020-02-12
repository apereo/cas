package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.CredentialMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.PrincipalMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationProviderBypassTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@DirtiesContext
@SpringBootTest(classes = AopAutoConfiguration.class)
@Tag("MFA")
public class MultifactorAuthenticationProviderBypassTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyMultifactorAuthenticationBypassByPrincipalAttributes() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("CAS");
        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal, CollectionUtils.wrap("authnFlag", "bypass"));
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val bypass = new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());

        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationAttributes() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationAttributeName("authnFlag");
        props.setAuthenticationAttributeValue("bypass");

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal, CollectionUtils.wrap("authnFlag", "bypass"));

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val bypass = new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationMethod() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationMethodName("simpleAuthentication");

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal,
            CollectionUtils.wrap(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, "simpleAuthentication"));

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val bypass = new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationHandler() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationHandlerName("SimpleAuthenticationHandler");

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal,
            CollectionUtils.wrap(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, "SimpleAuthenticationHandler"));

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val bypass = new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationCredentialClass() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setCredentialClassType(Credential.class.getName());

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val bypass = new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestHeader() {
        val request = new MockHttpServletRequest();
        request.addHeader("headerbypass", "true");
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestHeaders("headerbypass");

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val bypass = new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestRemoteAddress() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestRemoteAddress("123.+");

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val bypass = new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestRemoteHost() {
        val request = new MockHttpServletRequest();
        request.setRemoteHost("somewhere.example.org");
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestRemoteAddress(".+example\\.org");

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val bypass = new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByService() {
        val request = new MockHttpServletRequest();

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val bypass = new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(provider.getId());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();

        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.isBypassEnabled()).thenReturn(true);
        when(service.getMultifactorPolicy()).thenReturn(policy);

        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassIgnored() {
        val request = new MockHttpServletRequest();

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val bypass = new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(provider.getId());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertTrue(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }
}
