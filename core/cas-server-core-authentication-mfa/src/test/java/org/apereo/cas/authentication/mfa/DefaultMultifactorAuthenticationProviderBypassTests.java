package org.apereo.cas.authentication.mfa;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class
})
@DirtiesContext
public class DefaultMultifactorAuthenticationProviderBypassTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyMultifactorAuthenticationBypassByPrincipalAttributes() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MultifactorAuthenticationProviderBypassProperties props = new MultifactorAuthenticationProviderBypassProperties();
        props.setPrincipalAttributeName("givenName");
        props.setPrincipalAttributeValue("CAS");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final Principal principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal, CollectionUtils.wrap("authnFlag", "bypass"));

        final MultifactorAuthenticationProvider provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final RegisteredService service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationAttributes() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MultifactorAuthenticationProviderBypassProperties props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationAttributeName("authnFlag");
        props.setAuthenticationAttributeValue("bypass");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final Principal principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal, CollectionUtils.wrap("authnFlag", "bypass"));

        final MultifactorAuthenticationProvider provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final RegisteredService service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }


    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationMethod() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MultifactorAuthenticationProviderBypassProperties props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationMethodName("simpleAuthentication");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final Principal principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal,
            CollectionUtils.wrap(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, "simpleAuthentication"));

        final MultifactorAuthenticationProvider provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final RegisteredService service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationHandler() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MultifactorAuthenticationProviderBypassProperties props = new MultifactorAuthenticationProviderBypassProperties();
        props.setAuthenticationHandlerName("SimpleAuthenticationHandler");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final Principal principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("givenName", "CAS"));
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal,
            CollectionUtils.wrap(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, "SimpleAuthenticationHandler"));

        final MultifactorAuthenticationProvider provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final RegisteredService service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByAuthenticationCredentialClass() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MultifactorAuthenticationProviderBypassProperties props = new MultifactorAuthenticationProviderBypassProperties();
        props.setCredentialClassType(Credential.class.getName());
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final Principal principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final MultifactorAuthenticationProvider provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final RegisteredService service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestHeader() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("headerbypass", "true");
        final MultifactorAuthenticationProviderBypassProperties props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestHeaders("headerbypass");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final Principal principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final MultifactorAuthenticationProvider provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final RegisteredService service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestRemoteAddress() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        final MultifactorAuthenticationProviderBypassProperties props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestRemoteAddress("123.+");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final Principal principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final MultifactorAuthenticationProvider provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final RegisteredService service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByHttpRequestRemoteHost() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteHost("somewhere.example.org");
        final MultifactorAuthenticationProviderBypassProperties props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestRemoteAddress(".+example\\.org");
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final Principal principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final MultifactorAuthenticationProvider provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final RegisteredService service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassByService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MultifactorAuthenticationProviderBypassProperties props = new MultifactorAuthenticationProviderBypassProperties();
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final Principal principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final MultifactorAuthenticationProvider provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final RegisteredService service = MultifactorAuthenticationTestUtils.getRegisteredService();

        final RegisteredServiceMultifactorPolicy policy = mock(RegisteredServiceMultifactorPolicy.class);
        when(policy.isBypassEnabled()).thenReturn(true);
        when(service.getMultifactorPolicy()).thenReturn(policy);

        assertFalse(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }

    @Test
    public void verifyMultifactorAuthenticationBypassIgnored() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MultifactorAuthenticationProviderBypassProperties props = new MultifactorAuthenticationProviderBypassProperties();
        final MultifactorAuthenticationProviderBypass bypass = new DefaultMultifactorAuthenticationProviderBypass(props);

        final Principal principal = MultifactorAuthenticationTestUtils.getPrincipal("casuser");
        final Authentication authentication = MultifactorAuthenticationTestUtils.getAuthentication(principal);

        final MultifactorAuthenticationProvider provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        final RegisteredService service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertTrue(bypass.shouldMultifactorAuthenticationProviderExecute(authentication, service, provider, request));
    }
}
