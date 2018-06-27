package org.apereo.cas.authentication.mfa;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorAuthenticationProviderBypassTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@DirtiesContext
public class DefaultMultifactorAuthenticationProviderBypassTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyMultifactorAuthenticationBypassByPrincipalAttributes() {
        final var request = new MockHttpServletRequest();
        final var props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("CAS");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final var principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal, CollectionUtils.wrap("authnFlag", "bypass"));

        final var provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final var service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationAttributes() {
        final var request = new MockHttpServletRequest();
        final var props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationAttributeName("authnFlag");
        props.setAuthenticationAttributeValue("bypass");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final var principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal, CollectionUtils.wrap("authnFlag", "bypass"));

        final var provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final var service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }


    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationMethod() {
        final var request = new MockHttpServletRequest();
        final var props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationMethodName("simpleAuthentication");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final var principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal,
            CollectionUtils.wrap(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, "simpleAuthentication"));

        final var provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final var service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationHandler() {
        final var request = new MockHttpServletRequest();
        final var props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationHandlerName("SimpleAuthenticationHandler");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final var principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal,
            CollectionUtils.wrap(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, "SimpleAuthenticationHandler"));

        final var provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final var service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationCredentialClass() {
        final var request = new MockHttpServletRequest();
        final var props = new MultifactorAuthenticationProviderBypassProperties();
        props.setCredentialClassType(Credential.class.getName());
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final var principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final var provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final var service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestHeader() {
        final var request = new MockHttpServletRequest();
        request.addHeader("headerbypass", "true");
        final var props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestHeaders("headerbypass");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final var principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final var provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final var service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestRemoteAddress() {
        final var request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        final var props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestRemoteAddress("123.+");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final var principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final var provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final var service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestRemoteHost() {
        final var request = new MockHttpServletRequest();
        request.setRemoteHost("somewhere.example.org");
        final var props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestRemoteAddress(".+example\\.org");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final var principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final var provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final var service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByService() {
        final var request = new MockHttpServletRequest();
        final var props = new MultifactorAuthenticationProviderBypassProperties();
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final var principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final var provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final var service = MultifactorAuthenticationTestUtils.getRegisteredService();

        final var policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.isBypassEnabled()).thenReturn(true);
        when(service.getMultifactorPolicy()).thenReturn(policy);

        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassIgnored() {
        final var request = new MockHttpServletRequest();
        final var props = new MultifactorAuthenticationProviderBypassProperties();
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final var principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final var authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final var provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final var service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertTrue(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }
}
