package org.apereo.cas.support.saml.services;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlRegisteredServiceAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class SamlRegisteredServiceAttributeReleasePolicyTests {
    @Test
    public void verifyNoSamlService() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val policy = new EduPersonTargetedIdAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"), registeredService);
        assertTrue(attributes.isEmpty());
    }

    @Test
    public void verifyNoAppContext() {
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        val policy = new EduPersonTargetedIdAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"), registeredService);
        assertTrue(attributes.isEmpty());
    }

    @Test
    public void verifyBadHttpRequest() {
        val registeredService = new SamlRegisteredService();
        registeredService.setId(100);
        registeredService.setName("SAML");
        registeredService.setServiceId("https://sp.testshib.org/shibboleth-sp");
        registeredService.setMetadataLocation("classpath:metadata/testshib-providers.xml");

        val policy = new EduPersonTargetedIdAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService("https://sp.cas.org"), registeredService);
        assertTrue(attributes.isEmpty());
    }

    @Test
    public void verifyEntityIdAndService() {
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        val request = MockHttpServletRequest.class.cast(HttpRequestUtils.getHttpServletRequestFromRequestAttributes());
        Objects.requireNonNull(request).removeParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);

        val service = "https://example.org?" + SamlProtocolConstants.PARAMETER_ENTITY_ID + "=https://sp.testshib.org/shibboleth-sp";
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service);

        val policy = new EduPersonTargetedIdAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"), registeredService);
        assertTrue(attributes.isEmpty());
    }

    @Test
    public void verifyBadServiceProvider() {
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        when(resolver.resolve(any(), any())).thenReturn(mock(MetadataResolver.class));

        val ctx = mock(ConfigurableApplicationContext.class);
        val beanFactory = mock(AutowireCapableBeanFactory.class);
        when(beanFactory.getBean(CasConfigurationProperties.class)).thenReturn(new CasConfigurationProperties());
        when(ctx.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
        when(ctx.getBean("defaultSamlRegisteredServiceCachingMetadataResolver",
            SamlRegisteredServiceCachingMetadataResolver.class)).thenReturn(resolver);

        ApplicationContextProvider.holdApplicationContext(ctx);
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setServiceId("https://sp.cas.org");
        val policy = new EduPersonTargetedIdAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService("https://sp.cas.org"), registeredService);
        assertTrue(attributes.isEmpty());
    }
}
