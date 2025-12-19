package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.idp.metadata.cache.CachedMetadataResolverResult;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlRegisteredServiceAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLAttributes")
@ExtendWith(CasTestExtension.class)
class SamlRegisteredServiceAttributeReleasePolicyTests {
    @Nested
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        DefaultTests.SamlTestConfiguration.class
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    class DefaultTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyNoSamlService() throws Throwable {
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            val policy = new EduPersonTargetedIdAttributeReleasePolicy();
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .applicationContext(applicationContext)
                .service(CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
                .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
                .build();
            val attributes = policy.getAttributes(releasePolicyContext);
            assertTrue(attributes.isEmpty());
        }

        @Test
        void verifyBadHttpRequest() throws Throwable {
            val registeredService = new SamlRegisteredService();
            registeredService.setId(100);
            registeredService.setName("SAML");
            registeredService.setServiceId("https://sp.testshib.org/shibboleth-sp");
            registeredService.setMetadataLocation("classpath:metadata/testshib-providers.xml");

            val policy = new EduPersonTargetedIdAttributeReleasePolicy();
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .applicationContext(applicationContext)
                .service(CoreAuthenticationTestUtils.getService("https://sp.cas.org"))
                .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
                .build();
            val attributes = policy.getAttributes(releasePolicyContext);
            assertTrue(attributes.isEmpty());
        }

        @Test
        void verifyEntityIdAndService() throws Throwable {
            val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
            val request = (MockHttpServletRequest) HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            Objects.requireNonNull(request).removeParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);

            val service = "https://example.org?" + SamlProtocolConstants.PARAMETER_ENTITY_ID + "=https://sp.testshib.org/shibboleth-sp";
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service);

            val policy = new EduPersonTargetedIdAttributeReleasePolicy();
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .applicationContext(applicationContext)
                .service(CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
                .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
                .build();
            val attributes = policy.getAttributes(releasePolicyContext);
            assertFalse(attributes.isEmpty());
        }

        @Test
        void verifyWildcardEntityIdAndService() throws Throwable {
            val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
            registeredService.setServiceId(".+testshib.org.+");

            val request = (MockHttpServletRequest) HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            Objects.requireNonNull(request).removeParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID);

            val service = "https://example.org?" + SamlProtocolConstants.PARAMETER_ENTITY_ID + "=https://sp.testshib.org/shibboleth-sp";
            request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service);

            val policy = new EduPersonTargetedIdAttributeReleasePolicy();
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .applicationContext(applicationContext)
                .service(CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
                .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
                .build();
            val attributes = policy.getAttributes(releasePolicyContext);
            assertFalse(attributes.isEmpty());
        }


        @TestConfiguration(value = "SamlTestConfiguration", proxyBeanMethods = false)
        static class SamlTestConfiguration {
            @Bean
            public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver() throws Throwable {
                val mdResolver = mock(MetadataResolver.class);
                val entity = mock(EntityDescriptor.class);
                val sp = mock(SPSSODescriptor.class);
                when(entity.getSPSSODescriptor(anyString())).thenReturn(sp);
                when(mdResolver.resolveSingle(any())).thenReturn(entity);

                val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
                val resolverResult = CachedMetadataResolverResult.builder().metadataResolver(mdResolver).build();
                when(resolver.resolve(any(), any())).thenReturn(resolverResult);
                return resolver;
            }
        }

    }

    @Nested
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        NoServiceProvider.SamlTestConfiguration.class
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class NoServiceProvider {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyBadServiceProvider() throws Throwable {
            val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
            registeredService.setServiceId("https://sp.cas.org");
            val policy = new EduPersonTargetedIdAttributeReleasePolicy();

            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .applicationContext(applicationContext)
                .service(CoreAuthenticationTestUtils.getService(registeredService.getServiceId()))
                .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
                .build();
            val attributes = policy.getAttributes(releasePolicyContext);
            assertTrue(attributes.isEmpty());
        }

        @TestConfiguration(value = "SamlTestConfiguration", proxyBeanMethods = false)
        static class SamlTestConfiguration {
            @Bean
            public SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver() throws Exception {
                val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
                val resolverResult = CachedMetadataResolverResult.builder().metadataResolver(mock(MetadataResolver.class)).build();
                when(resolver.resolve(any(), any())).thenReturn(resolverResult);
                return resolver;
            }
        }
    }
}
