package org.apereo.cas.support.saml.web.idp.profile.builders.assertion;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlAssertionBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
public class SamlProfileSamlAssertionBuilderTests {

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class AssertionWithDefaultMetadata extends BaseSamlIdPConfigurationTests {
        @Autowired
        @Qualifier("samlProfileSamlAssertionBuilder")
        private SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder;

        @Test
        public void verifyAssertionWithDefaultIssuer() {
            val service = getSamlRegisteredServiceForTestShib();
            val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
                .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
            val assertion = samlProfileSamlAssertionBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
                new MockHttpServletResponse(), getAssertion(), service,
                adaptor, SAMLConstants.SAML2_POST_BINDING_URI, new MessageContext());
            assertEquals(casProperties.getAuthn().getSamlIdp().getCore().getEntityId(),
                SamlIdPUtils.getIssuerFromSamlObject(assertion));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=classpath:metadata/")
    public class AssertionWithServiceProviderMetadata extends BaseSamlIdPConfigurationTests {
        @Autowired
        @Qualifier("samlProfileSamlAssertionBuilder")
        private SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder;

        @Test
        public void verifyAssertionWithServiceMetadataAndIssuer() {
            val service = getSamlRegisteredServiceFor("https://cassp.example.org");
            service.setId(1000);
            service.setName("ObjectSignerTest");

            val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
                .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
            val assertion = samlProfileSamlAssertionBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
                new MockHttpServletResponse(), getAssertion(), service,
                adaptor, SAMLConstants.SAML2_POST_BINDING_URI, new MessageContext());
            assertEquals("https://cas.example.org/customidp", SamlIdPUtils.getIssuerFromSamlObject(assertion));
        }
    }


}
