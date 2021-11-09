package org.apereo.cas.support.saml.util;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
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
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAML")
public class SamlIdPUtilsTests extends BaseSamlIdPConfigurationTests {

    @BeforeEach
    public void before() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyMetadataForAllServices() throws Exception {
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
    public void verifyUnsignedRequestWithAssertionConsumerServiceUrlNotMatchingMetadataAcsUrl() {
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
        val adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        assertThrows(SamlException.class, () -> SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context),
            adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI));
    }

    @Test
    public void verifyUnsignedRequestWithAssertionConsumerServiceUrlNotMatchingMetadataAcsUrlWithIndex() {
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
        val adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals("https://sp.testshib.org/Shibboleth.sso/SAML2/POST", acs.getResponseLocation());
        assertEquals("https://sp.testshib.org/Shibboleth.sso/SAML2/POST", acs.getLocation());
    }

    @Test
    public void verifySignedRequestWithAssertionConsumerServiceUrlNotMatchingMetadataAcsUrl() {
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

        val adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, new MessageContext()), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals(acsUrl, acs.getLocation());
    }

    @Test
    public void verifySignedRequestWithAssertionConsumerServiceIndex() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(9);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.isSigned()).thenReturn(true);

        val adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, new MessageContext()), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals("https://index9.testshib.org/Shibboleth.sso/SAML/POST", acs.getLocation());
        assertEquals(9, ((AssertionConsumerService) acs).getIndex());
    }

    @Test
    public void verifyUnsignedRequestWithAssertionConsumerServiceIndex() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(9);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.isSigned()).thenReturn(false);

        val adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val context = new MessageContext();
        context.setMessage(authnRequest);
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals("https://index9.testshib.org/Shibboleth.sso/SAML/POST", acs.getLocation());
        assertEquals(9, ((AssertionConsumerService) acs).getIndex());
    }

    @Test
    public void verifySignedRequestWithAssertionConsumerServiceUnknownIndex() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(999);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.isSigned()).thenReturn(true);

        val adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, new MessageContext()),
            adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals("https://www.testshib.org/Shibboleth.sso/SAML2/POST", acs.getLocation());
        assertEquals(7, ((AssertionConsumerService) acs).getIndex());
    }

    @Test
    public void verifySignedRequestWithEmbeddedSignature() {
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
        val binding = context.getSubcontext(SAMLBindingContext.class, true);
        binding.setHasBindingSignature(true);
        binding.setRelayState(UUID.randomUUID().toString());

        val adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals(acsUrl, acs.getLocation());
    }

    @Test
    public void verifyUnsignedRequestWithAssertionConsumerServiceUrlMatchingMetadataAcsUrl() {
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
        val adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals(acsUrl, acs.getLocation());
    }

    @Test
    public void verifyUnsignedRequestWithAssertionConsumerServiceUrlMatchingAlternateMetadataAcsUrl() {
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
        val adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, context), adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals(acsUrl, acs.getLocation());
    }

    @Test
    public void verifyPreparePeerEntitySamlEndpointContext() {
        val context = new MessageContext();
        val adaptor = mock(SamlRegisteredServiceServiceProviderMetadataFacade.class);

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
