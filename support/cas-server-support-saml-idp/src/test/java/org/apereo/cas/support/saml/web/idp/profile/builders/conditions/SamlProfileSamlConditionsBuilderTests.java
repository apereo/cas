package org.apereo.cas.support.saml.web.idp.profile.builders.conditions;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlConditionsBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAMLResponse")
class SamlProfileSamlConditionsBuilderTests {
    abstract static class BaseTests extends BaseSamlIdPConfigurationTests {
        protected SamlProfileBuilderContext getSamlProfileBuilderContext(final SamlRegisteredService service) {
            val adaptor = SamlRegisteredServiceMetadataAdaptor.get(
                samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

            return SamlProfileBuilderContext.builder()
                .samlRequest(getAuthnRequestFor(service))
                .httpRequest(new MockHttpServletRequest())
                .httpResponse(new MockHttpServletResponse())
                .authenticatedAssertion(Optional.of(getAssertion()))
                .registeredService(service)
                .adaptor(adaptor)
                .binding(SAMLConstants.SAML2_POST_BINDING_URI)
                .build();
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.response.skew-allowance=3000",
        "cas.saml-core.skew-allowance=3000"
    })
    class RegisteredServiceSkewTests extends BaseTests {
        @Test
        void verifyWithSkewForService() throws Throwable {
            val service = getSamlRegisteredServiceForTestShib(true, true);
            service.setAssertionAudiences("https://www.example.com");
            service.setSkewAllowance(3600);

            val buildContext = getSamlProfileBuilderContext(service);
            val result = samlProfileSamlConditionsBuilder.build(buildContext);
            assertNotNull(result);
            val diff = Duration.between(result.getNotBefore(), result.getNotOnOrAfter()).toSeconds();
            assertEquals((long) service.getSkewAllowance() << 1, diff);
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.saml-idp.response.skew-allowance=PT5S")
    class SamlIdPResponseSkewTests extends BaseTests {
        @Test
        void verifyWithSkewForService() throws Throwable {
            val service = getSamlRegisteredServiceForTestShib(true, true);
            val buildContext = getSamlProfileBuilderContext(service);
            val result = samlProfileSamlConditionsBuilder.build(buildContext);
            assertNotNull(result);
            val diff = Duration.between(result.getNotBefore(), result.getNotOnOrAfter()).toSeconds();
            assertEquals(10, diff);
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.response.skew-allowance=0",
        "cas.saml-core.skew-allowance=PT2S"
    })
    class SamlCoreSkewTests extends BaseTests {
        @Test
        void verifyWithSkewForService() throws Throwable {
            val service = getSamlRegisteredServiceForTestShib(true, true);
            val buildContext = getSamlProfileBuilderContext(service);
            val result = samlProfileSamlConditionsBuilder.build(buildContext);
            assertNotNull(result);
            val diff = Duration.between(result.getNotBefore(), result.getNotOnOrAfter()).toSeconds();
            assertEquals(4, diff);
        }
    }
}
