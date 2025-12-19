package org.apereo.cas.support.saml.web.idp.profile.builders.assertion;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
@Tag("SAMLResponse")
class SamlProfileSamlAssertionBuilderTests {

    @Nested
    class AssertionWithDefaultMetadata extends BaseSamlIdPConfigurationTests {
        @Autowired
        @Qualifier("samlProfileSamlAssertionBuilder")
        private SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder;

        @Test
        void verifyAssertionWithDefaultIssuer() throws Throwable {
            val service = getSamlRegisteredServiceForTestShib();
            val adaptor = SamlRegisteredServiceMetadataAdaptor.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId())
                .orElseThrow(() -> new AssertionError("Unable to locate metadata for %s".formatted(service.getServiceId())));

            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(getAuthnRequestFor(service))
                .httpRequest(new MockHttpServletRequest())
                .httpResponse(new MockHttpServletResponse())
                .authenticatedAssertion(Optional.of(getAssertion()))
                .registeredService(service)
                .adaptor(adaptor)
                .binding(SAMLConstants.SAML2_POST_BINDING_URI)
                .build();

            val assertion = samlProfileSamlAssertionBuilder.build(buildContext);
            assertEquals(casProperties.getAuthn().getSamlIdp().getCore().getEntityId(),
                SamlIdPUtils.getIssuerFromSamlObject(assertion));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=classpath:metadata/")
    class AssertionWithServiceProviderMetadata extends BaseSamlIdPConfigurationTests {
        @Autowired
        @Qualifier("samlProfileSamlAssertionBuilder")
        private SamlProfileObjectBuilder<Assertion> samlProfileSamlAssertionBuilder;

        @Test
        void verifyAssertionWithServiceMetadataAndIssuer() throws Throwable {
            val service = getSamlRegisteredServiceFor("https://cassp.example.org");
            service.setId(1000);
            service.setName("ObjectSignerTest");

            val adaptor = SamlRegisteredServiceMetadataAdaptor
                .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();

            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(getAuthnRequestFor(service))
                .httpRequest(new MockHttpServletRequest())
                .httpResponse(new MockHttpServletResponse())
                .authenticatedAssertion(Optional.of(getAssertion()))
                .registeredService(service)
                .adaptor(adaptor)
                .binding(SAMLConstants.SAML2_POST_BINDING_URI)
                .build();

            val assertion = samlProfileSamlAssertionBuilder.build(buildContext);
            assertEquals("https://cas.example.org/customidp", SamlIdPUtils.getIssuerFromSamlObject(assertion));
        }
    }
    
}
