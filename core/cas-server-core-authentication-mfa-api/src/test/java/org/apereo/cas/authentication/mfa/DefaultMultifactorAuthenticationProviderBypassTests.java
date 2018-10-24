package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorAuthenticationProviderBypassTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@DirtiesContext
public class DefaultMultifactorAuthenticationProviderBypassTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyMultifactorAuthenticationBypassByPrincipalAttributes() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("CAS");
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal, CollectionUtils.wrap("authnFlag", "bypass"));

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationAttributes() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationAttributeName("authnFlag");
        props.setAuthenticationAttributeValue("bypass");
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal, CollectionUtils.wrap("authnFlag", "bypass"));

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }


    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationMethod() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationMethodName("simpleAuthentication");
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal,
            CollectionUtils.wrap(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, "simpleAuthentication"));

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationHandler() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationHandlerName("SimpleAuthenticationHandler");
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal,
            CollectionUtils.wrap(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, "SimpleAuthenticationHandler"));

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationCredentialClass() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setCredentialClassType(Credential.class.getName());
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestHeader() {
        val request = new MockHttpServletRequest();
        request.addHeader("headerbypass", "true");
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestHeaders("headerbypass");
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestRemoteAddress() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestRemoteAddress("123.+");
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestRemoteHost() {
        val request = new MockHttpServletRequest();
        request.setRemoteHost("somewhere.example.org");
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestRemoteAddress(".+example\\.org");
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByService() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();

        val policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.isBypassEnabled()).thenReturn(true);
        when(service.getMultifactorPolicy()).thenReturn(policy);

        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassIgnored() {
        val request = new MockHttpServletRequest();
        val props = new MultifactorAuthenticationProviderBypassProperties();
        val bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        val principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        val authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertTrue(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }
}
