package org.apereo.cas.support.saml.util;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.BindingCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.metadata.IndexedEndpoint;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAML2")
class SamlIdPUtilsTests extends BaseSamlIdPConfigurationTests {

    @BeforeEach
    void before() {
        servicesManager.deleteAll();
    }

    @Test
    void verifyServiceNameQualifier() {
        val service = getSamlRegisteredServiceForTestShib();
        val nameIdQualifier = UUID.randomUUID().toString();
        service.setNameIdQualifier(nameIdQualifier);
        servicesManager.save(service);
        assertEquals(nameIdQualifier, SamlIdPUtils.determineNameIdNameQualifier(service, mock(MetadataResolver.class)));
    }

    @Test
    void verifyEndpointWithoutLocation() {
        val logoutRequest = mock(LogoutRequest.class);
        val endpoint = mock(SingleLogoutService.class);
        val adaptor = mock(SamlRegisteredServiceMetadataAdaptor.class);
        when(adaptor.getSingleLogoutService(anyString())).thenReturn(endpoint);
        assertThrows(SamlException.class, () -> SamlIdPUtils.determineEndpointForRequest(Pair.of(logoutRequest, new MessageContext()),
            adaptor, SAMLConstants.SAML2_POST_BINDING_URI));
    }

    @Test
    void verifyMetadataForAllServices() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val md = SamlIdPUtils.getMetadataResolverForAllSamlServices(servicesManager, service.getServiceId(),
            samlRegisteredServiceCachingMetadataResolver);
        assertNotNull(md);

        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion(service.getServiceId()));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteriaSet.add(new BindingCriterion(CollectionUtils.wrap(SAMLConstants.SAML2_POST_BINDING_URI)));
        val it = md.resolve(criteriaSet).iterator();
        assertTrue(it.hasNext());
        assertEquals(service.getServiceId(), it.next().getEntityID());
    }


    @Test
    void verifyUnsignedRequestWithAssertionConsumerServiceUrlNotMatchingMetadataAcsUrl() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        val acsUrl = "https://some.acs.url";
        when(authnRequest.getAssertionConsumerServiceURL()).thenReturn(acsUrl);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(120);

        val context = new MessageContext();
        context.setMessage(authnRequest);
        val adapter = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        assertThrows(SamlException.class, () -> SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context),
            adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI));
    }

    @Test
    void verifyUnsignedRequestWithAssertionConsumerServiceUrlNotMatchingMetadataAcsUrlWithIndex() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        val acsUrl = "https://some.acs.url";
        when(authnRequest.getAssertionConsumerServiceURL()).thenReturn(acsUrl);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(1);

        val context = new MessageContext();
        context.setMessage(authnRequest);
        val adapter = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals("https://sp.testshib.org/Shibboleth.sso/SAML2/POST", acs.getResponseLocation());
        assertEquals("https://sp.testshib.org/Shibboleth.sso/SAML2/POST", acs.getLocation());
    }

    @Test
    void verifySignedRequestWithAssertionConsumerServiceUrlNotMatchingMetadataAcsUrl() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.isSigned()).thenReturn(true);
        val acsUrl = "https://some.acs.url";
        when(authnRequest.getAssertionConsumerServiceURL()).thenReturn(acsUrl);

        val adapter = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, new MessageContext()), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals(acsUrl, acs.getLocation());
    }

    @Test
    void verifySignedRequestWithAssertionConsumerServiceIndex() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(9);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.isSigned()).thenReturn(true);

        val adapter = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, new MessageContext()), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals("https://index9.testshib.org/Shibboleth.sso/SAML/POST", acs.getLocation());
        assertEquals(9, ((IndexedEndpoint) acs).getIndex());
    }

    @Test
    void verifyUnsignedRequestWithAssertionConsumerServiceIndex() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(9);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.isSigned()).thenReturn(false);

        val adapter = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val context = new MessageContext();
        context.setMessage(authnRequest);
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals("https://index9.testshib.org/Shibboleth.sso/SAML/POST", acs.getLocation());
        assertEquals(9, ((IndexedEndpoint) acs).getIndex());
    }

    @Test
    void verifySignedRequestWithAssertionConsumerServiceUnknownIndex() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(999);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.isSigned()).thenReturn(true);

        val adapter = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, new MessageContext()),
            adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals("https://www.testshib.org/Shibboleth.sso/SAML2/POST", acs.getLocation());
        assertEquals(7, ((IndexedEndpoint) acs).getIndex());
    }

    @Test
    void verifySignedRequestWithEmbeddedSignature() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        val acsUrl = "https://sp.unknown.org/Shibboleth.sso/SAML2/POST";
        when(authnRequest.getAssertionConsumerServiceURL()).thenReturn(acsUrl);

        val context = new MessageContext();
        context.setMessage(authnRequest);
        val binding = context.ensureSubcontext(SAMLBindingContext.class);
        binding.setHasBindingSignature(true);
        binding.setRelayState(UUID.randomUUID().toString());

        val adapter = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals(acsUrl, acs.getLocation());
    }

    @Test
    void verifyUnsignedRequestWithAssertionConsumerServiceUrlMatchingMetadataAcsUrl() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        val acsUrl = "https://sp.testshib.org/Shibboleth.sso/SAML2/POST";
        when(authnRequest.getAssertionConsumerServiceURL()).thenReturn(acsUrl);

        val context = new MessageContext();
        context.setMessage(authnRequest);
        val adapter = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals(acsUrl, acs.getLocation());
    }

    @Test
    void verifyUnsignedRequestWithAssertionConsumerServiceUrlMatchingAlternateMetadataAcsUrl() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        val acsUrl = "https://www.testshib.org/Shibboleth.sso/SAML2/POST";
        when(authnRequest.getAssertionConsumerServiceURL()).thenReturn(acsUrl);

        val context = new MessageContext();
        context.setMessage(authnRequest);
        val adapter = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals(acsUrl, acs.getLocation());
    }

    @Test
    void verifyPreparePeerEntitySamlEndpointContext() {
        val context = new MessageContext();
        val adaptor = mock(SamlRegisteredServiceMetadataAdaptor.class);

        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);

        when(adaptor.containsAssertionConsumerServices()).thenReturn(false);
        assertThrows(SamlException.class,
            () -> SamlIdPUtils.preparePeerEntitySamlEndpointContext(Pair.of(authnRequest, context), context, adaptor, SAMLConstants.SAML2_POST_BINDING_URI));

        when(adaptor.containsAssertionConsumerServices()).thenReturn(true);
        when(authnRequest.isSigned()).thenReturn(true);
        assertThrows(SamlException.class,
            () -> SamlIdPUtils.preparePeerEntitySamlEndpointContext(Pair.of(authnRequest, context), context, adaptor, SAMLConstants.SAML2_POST_BINDING_URI));
    }
}
