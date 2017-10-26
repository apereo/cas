package org.apereo.cas.support.saml;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.ExternalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        ExternalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration.class})
@TestPropertySource(properties = "cas.authn.shibIdp.serverUrl=https://idp.example.com")
public class ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategyTests {

    @Autowired
    @Qualifier("shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy;

    @Test
    public void verifyServiceNotFound() {
        final Service svc = RegisteredServiceTestUtils.getService("https://www.example.org?param1=value1");
        final Service result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals(svc.getId(), result.getId());
        assertFalse(shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.supports(svc));
    }
    
    @Test
    public void verifyServiceFound() {
        final Service svc = RegisteredServiceTestUtils.getService("https://www.example.org?entityId=https://idp.example.org");
        final Service result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals(result.getId(), "https://idp.example.org");
    }

    @Test
    public void verifyServiceFoundEncoded() {
        final String serviceUrl = "https%3A%2F%2Fidp.example.com%2Fidp%2FAuthn%2FExtCas%3Fconversation%3De1s1&entityId=https%3A%2F%2Fservice.example.com";
        final Service svc = RegisteredServiceTestUtils.getService(
                "https://cas.example.com/login?service=" + serviceUrl);
        final Service result = shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy.resolveServiceFrom(svc);
        assertEquals(result.getId(), "https://service.example.com");
    }

}
