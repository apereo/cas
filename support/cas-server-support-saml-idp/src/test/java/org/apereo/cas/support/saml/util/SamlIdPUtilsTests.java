package org.apereo.cas.support.saml.util;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.BindingCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("filesystem")
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
    public void verifyAssertionConsumerServiceNoIndex() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);

        val authnRequest = mock(AuthnRequest.class);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(null);
        when(authnRequest.getAssertionConsumerServiceURL()).thenReturn("https://sp.testshib.org/Shibboleth.sso/SAML/POST");
        val acs = SamlIdPUtils.getAssertionConsumerServiceFor(authnRequest, servicesManager,
            samlRegisteredServiceCachingMetadataResolver);
        assertNotNull(acs);
    }

    @Test
    public void verifyAssertionConsumerServiceWithIndex() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);

        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(0);
        val acs = SamlIdPUtils.getAssertionConsumerServiceFor(authnRequest, servicesManager,
            samlRegisteredServiceCachingMetadataResolver);
        assertNotNull(acs);
    }

    @Test
    public void verifyAssertionConsumerServiceWithUrl() {
        val service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        val acsUrl = "https://some.acs.url";
        when(authnRequest.getAssertionConsumerServiceURL()).thenReturn(acsUrl);

        val adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        val acs = SamlIdPUtils.determineEndpointForRequest(authnRequest, adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals(acsUrl, acs.getLocation());
    }
}
