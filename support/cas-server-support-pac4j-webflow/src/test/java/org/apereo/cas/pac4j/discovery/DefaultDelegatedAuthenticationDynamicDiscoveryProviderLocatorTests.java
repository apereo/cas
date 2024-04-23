package org.apereo.cas.pac4j.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Delegation")
class DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocatorTests {

    @SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @TestPropertySource(properties = {
        "cas.authn.pac4j.core.discovery-selection.selection-type=DYNAMIC",
        "cas.authn.pac4j.core.discovery-selection.json.location=classpath:delegated-discovery.json",
        "cas.authn.pac4j.core.discovery-selection.json.principal-attribute=mail"
    })
    abstract static class BaseTests {
        @Autowired
        @Qualifier("delegatedAuthenticationDynamicDiscoveryProviderLocator")
        protected DelegatedAuthenticationDynamicDiscoveryProviderLocator delegatedAuthenticationDynamicDiscoveryProviderLocator;
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.attribute-repository.stub.attributes.mail=casuser@apereo.org")
    class WithPrincipalResolution extends BaseTests {
        @Test
        void verifyPrincipalAttribute() throws Throwable {
            val request = DelegatedAuthenticationDynamicDiscoveryProviderLocator.DynamicDiscoveryProviderRequest.builder().userId("casuser").build();
            assertTrue(delegatedAuthenticationDynamicDiscoveryProviderLocator.locate(request).isPresent());
        }
    }

    @Nested
    class WithoutPrincipalResolution extends BaseTests {
        @Test
        void verifyUnknownPrincipal() throws Throwable {
            val request = DelegatedAuthenticationDynamicDiscoveryProviderLocator.DynamicDiscoveryProviderRequest.builder().userId("cas@unknown.org").build();
            assertTrue(delegatedAuthenticationDynamicDiscoveryProviderLocator.locate(request).isEmpty());
        }
    }
}
