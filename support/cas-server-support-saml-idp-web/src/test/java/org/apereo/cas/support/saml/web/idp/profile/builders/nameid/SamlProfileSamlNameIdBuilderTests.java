package org.apereo.cas.support.saml.web.idp.profile.builders.nameid;

import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlProfileSamlNameIdBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CoreSamlConfiguration.class
})
public class SamlProfileSamlNameIdBuilderTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Test
    public void verifyAction() {
        val b = new SamlProfileSamlNameIdBuilder(openSamlConfigBean, new ShibbolethCompatiblePersistentIdGenerator());
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);

        val policy = mock(NameIDPolicy.class);
        when(policy.getFormat()).thenReturn(NameID.EMAIL);
        when(authnRequest.getNameIDPolicy()).thenReturn(policy);

        val service = new SamlRegisteredService();
        service.setServiceId("entity-id");
        service.setRequiredNameIdFormat(NameID.EMAIL);
        val facade = mock(SamlRegisteredServiceServiceProviderMetadataFacade.class);
        when(facade.getEntityId()).thenReturn(service.getServiceId());
        val assertion = mock(Assertion.class);
        when(assertion.getPrincipal()).thenReturn(new AttributePrincipalImpl("casuser"));

        when(facade.getSupportedNameIdFormats()).thenReturn(CollectionUtils.wrapList(NameID.TRANSIENT, NameID.EMAIL));
        val result = b.build(authnRequest, new MockHttpServletRequest(), new MockHttpServletResponse(),
            assertion, service, facade, SAMLConstants.SAML2_POST_BINDING_URI, mock(MessageContext.class));
        assertNotNull(result);
        assertEquals(NameID.EMAIL, result.getFormat());
        assertEquals("casuser", result.getValue());
    }
}
