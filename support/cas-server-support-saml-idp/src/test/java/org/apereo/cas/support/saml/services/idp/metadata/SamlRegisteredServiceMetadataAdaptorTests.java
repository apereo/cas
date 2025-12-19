package org.apereo.cas.support.saml.services.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.CachedMetadataResolverResult;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlRegisteredServiceMetadataAdaptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLMetadata")
class SamlRegisteredServiceMetadataAdaptorTests extends BaseSamlIdPConfigurationTests {

    @Test
    void verifyResolver() {
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver,
            service, authnRequest).get();
        assertNotNull(adaptor);
        assertNull(adaptor.getValidUntil());
        assertNull(adaptor.getOrganization());
        assertNull(adaptor.getSignature());
        assertNotNull(adaptor.getContactPersons());
        assertNull(adaptor.getCacheDuration());
        assertNotNull(adaptor.getKeyDescriptors());
        assertNotNull(adaptor.getExtensions());
        assertNotNull(adaptor.getSupportedProtocols());
        assertNotNull(adaptor.getSingleLogoutService());
        assertNotNull(adaptor.getAssertionConsumerServiceLocations());
        assertNull(adaptor.getAssertionConsumerServiceForPaosBinding());
        val acs = adaptor.getAssertionConsumerServiceForPostBinding();
        assertNotNull(acs);
        assertEquals(7, acs.getIndex());
        assertNotNull(adaptor.getAssertionConsumerServiceForArtifactBinding());
        assertTrue(adaptor.assertionConsumerServicesSize() > 0);
        assertFalse(adaptor.isWantAssertionsSigned());
        assertFalse(adaptor.isAuthnRequestsSigned());
        assertFalse(adaptor.isSupportedProtocol("example"));
        assertFalse(adaptor.isSupportedProtocol("example"));
        assertTrue(adaptor.getAssertionConsumerServiceLocations(SAMLConstants.SAML2_POST_BINDING_URI).size() > 1);
    }

    @Test
    void verifyResolverNoEntityDesc() throws Throwable {
        val mdr = mock(MetadataResolver.class);
        when(mdr.resolve(any())).thenReturn(null);
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        when(resolver.resolve(any(SamlRegisteredService.class), any()))
            .thenReturn(CachedMetadataResolverResult.builder().metadataResolver(mdr).build());
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        assertTrue(SamlRegisteredServiceMetadataAdaptor.get(resolver, service, authnRequest).isEmpty());
    }

    @Test
    void verifyNoSsoDescriptor() throws Throwable {
        val mdr = mock(MetadataResolver.class);
        val entityDesc = mock(EntityDescriptor.class);
        when(mdr.resolve(any())).thenReturn(List.of(entityDesc));
        when(mdr.resolveSingle(any())).thenReturn(entityDesc);
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        when(resolver.resolve(any(SamlRegisteredService.class), any()))
            .thenReturn(CachedMetadataResolverResult.builder().metadataResolver(mdr).build());
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        assertTrue(SamlRegisteredServiceMetadataAdaptor.get(resolver, service, authnRequest).isEmpty());
    }

    @Test
    void verifyResolverExpiredEntity() throws Throwable {
        val entityDesc = mock(EntityDescriptor.class);
        when(entityDesc.getValidUntil()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1).toInstant());
        val mdr = mock(MetadataResolver.class);
        when(mdr.resolveSingle(any())).thenReturn(entityDesc);
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        when(resolver.resolve(any(SamlRegisteredService.class), any()))
            .thenReturn(CachedMetadataResolverResult.builder().metadataResolver(mdr).build());
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        assertTrue(SamlRegisteredServiceMetadataAdaptor.get(resolver, service, authnRequest).isEmpty());
    }

    @Test
    void verifyResolverSpExpiredEntity() throws Throwable {
        val spDesc = mock(SPSSODescriptor.class);
        when(spDesc.getValidUntil()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1).toInstant());
        val entityDesc = mock(EntityDescriptor.class);
        when(entityDesc.getSPSSODescriptor(SAMLConstants.SAML20P_NS)).thenReturn(spDesc);

        val mdr = mock(MetadataResolver.class);
        when(mdr.resolveSingle(any())).thenReturn(entityDesc);
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        when(resolver.resolve(any(SamlRegisteredService.class), any()))
            .thenReturn(CachedMetadataResolverResult.builder().metadataResolver(mdr).build());
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        assertTrue(SamlRegisteredServiceMetadataAdaptor.get(resolver, service, authnRequest).isEmpty());
    }

}
