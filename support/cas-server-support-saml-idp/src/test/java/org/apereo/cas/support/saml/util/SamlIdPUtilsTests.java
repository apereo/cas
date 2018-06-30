package org.apereo.cas.support.saml.util;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.Before;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.BindingCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(FileSystemCategory.class)
public class SamlIdPUtilsTests extends BaseSamlIdPConfigurationTests {

    @Before
    public void before() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyMetadataForAllServices() throws Exception {
        final var service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        final var md = SamlIdPUtils.getMetadataResolverForAllSamlServices(servicesManager, service.getServiceId(),
            samlRegisteredServiceCachingMetadataResolver);
        assertNotNull(md);

        final var criteriaSet = new CriteriaSet();
        criteriaSet.add(new EntityIdCriterion(service.getServiceId()));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteriaSet.add(new BindingCriterion(CollectionUtils.wrap(SAMLConstants.SAML2_POST_BINDING_URI)));
        final var it = md.resolve(criteriaSet).iterator();
        assertTrue(it.hasNext());
        assertEquals(service.getServiceId(), it.next().getEntityID());
    }

    @Test
    public void verifyAssertionConsumerServiceNoIndex() {
        final var service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);

        final var authnRequest = mock(AuthnRequest.class);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(null);
        when(authnRequest.getAssertionConsumerServiceURL()).thenReturn("https://sp.testshib.org/Shibboleth.sso/SAML/POST");
        final var acs = SamlIdPUtils.getAssertionConsumerServiceFor(authnRequest, servicesManager,
            samlRegisteredServiceCachingMetadataResolver);
        assertNotNull(acs);
    }

    @Test
    public void verifyAssertionConsumerServiceWithIndex() {
        final var service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);

        final var authnRequest = mock(AuthnRequest.class);
        final var issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        when(authnRequest.getAssertionConsumerServiceIndex()).thenReturn(0);
        final var acs = SamlIdPUtils.getAssertionConsumerServiceFor(authnRequest, servicesManager,
            samlRegisteredServiceCachingMetadataResolver);
        assertNotNull(acs);
    }

    @Test
    public void verifyAssertionConsumerServiceWithUrl() {
        final var service = getSamlRegisteredServiceForTestShib();
        servicesManager.save(service);
        final var authnRequest = mock(AuthnRequest.class);
        final var issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn(service.getServiceId());
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn(SAMLConstants.SAML2_POST_BINDING_URI);
        final var acsUrl = "https://some.acs.url";
        when(authnRequest.getAssertionConsumerServiceURL()).thenReturn(acsUrl);

        final var adapter = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId());
        final var acs = SamlIdPUtils.determineAssertionConsumerService(authnRequest, adapter.get(), SAMLConstants.SAML2_POST_BINDING_URI);
        assertNotNull(acs);
        assertEquals(acsUrl, acs.getLocation());
    }
}
