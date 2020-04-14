package org.apereo.cas.support.saml.services.idp.metadata;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlRegisteredServiceServiceProviderMetadataFacadeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class SamlRegisteredServiceServiceProviderMetadataFacadeTests extends BaseSamlIdPConfigurationTests {

    @Test
    public void verifyResolver() {
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, authnRequest).get();
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
        assertNotNull(adaptor.getAssertionConsumerServiceForPostBinding());
        assertNotNull(adaptor.getAssertionConsumerServiceForArtifactBinding());
        assertTrue(adaptor.assertionConsumerServicesSize() > 0);
        assertFalse(adaptor.isWantAssertionsSigned());
        assertFalse(adaptor.isAuthnRequestsSigned());
        assertFalse(adaptor.isSupportedProtocol("example"));
        assertFalse(adaptor.isSupportedProtocol("example"));
    }

    @Test
    public void verifyResolverNoEntityDesc() throws Exception {
        val mdr = mock(MetadataResolver.class);
        when(mdr.resolve(any())).thenReturn(null);
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        when(resolver.resolve(any(SamlRegisteredService.class), any())).thenReturn(mdr);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        assertTrue(SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, service, authnRequest).isEmpty());
    }

    @Test
    public void verifyResolverExpiredEntity() throws Exception {
        val entityDesc = mock(EntityDescriptor.class);
        when(entityDesc.getValidUntil()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1).toInstant());
        val mdr = mock(MetadataResolver.class);
        when(mdr.resolveSingle(any())).thenReturn(entityDesc);
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        when(resolver.resolve(any(SamlRegisteredService.class), any())).thenReturn(mdr);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        assertTrue(SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, service, authnRequest).isEmpty());
    }

    @Test
    public void verifyResolverSpExpiredEntity() throws Exception {
        val spDesc = mock(SPSSODescriptor.class);
        when(spDesc.getValidUntil()).thenReturn(ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1).toInstant());
        val entityDesc = mock(EntityDescriptor.class);
        when(entityDesc.getSPSSODescriptor(SAMLConstants.SAML20P_NS)).thenReturn(spDesc);

        val mdr = mock(MetadataResolver.class);
        when(mdr.resolveSingle(any())).thenReturn(entityDesc);
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        when(resolver.resolve(any(SamlRegisteredService.class), any())).thenReturn(mdr);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        assertTrue(SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, service, authnRequest).isEmpty());
    }

}
