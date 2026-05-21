package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import module java.base;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlAuthNStatementBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAMLResponse")
class SamlProfileSamlAuthNStatementBuilderTests {

    private abstract static class BaseTests extends BaseSamlIdPConfigurationTests {
        @Autowired
        @Qualifier("samlProfileSamlAuthNStatementBuilder")
        private SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder;

        void checkResultAndValidity(final SamlRegisteredService service,
                                            final String expectedValidity) throws Exception {
            val adaptor = SamlRegisteredServiceMetadataAdaptor.get(
                samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).orElseThrow();

            val request = getAuthnRequestFor(UUID.randomUUID().toString());
            val buildContext = SamlProfileBuilderContext.builder()
                .samlRequest(request)
                .httpRequest(new MockHttpServletRequest())
                .httpResponse(new MockHttpServletResponse())
                .authenticatedAssertion(Optional.of(getAssertion()))
                .registeredService(service)
                .adaptor(adaptor)
                .binding(SAMLConstants.SAML2_POST_BINDING_URI)
                .build();

            val result = samlProfileSamlAuthNStatementBuilder.build(buildContext);
            assertNotNull(result);
            val sessionNotOnOrAfter = result.getSessionNotOnOrAfter();
            assertNotNull(sessionNotOnOrAfter);
            val now = ZonedDateTime.now(ZoneOffset.UTC);
            assertTrue(sessionNotOnOrAfter.isAfter(now.plusMinutes(Beans.newDuration(expectedValidity)
                .minusMinutes(1).toMinutes()).toInstant()));
            assertTrue(sessionNotOnOrAfter.isBefore(now.plusMinutes(Beans.newDuration(expectedValidity)
                .plusMinutes(1).toMinutes()).toInstant()));
        }
    }

    @Nested
    class DefaultTests extends BaseTests {
        @Test
        void verifyOperationDefaultSettings() throws Throwable {
            val service = getSamlRegisteredServiceForTestShib(true, true);
            checkResultAndValidity(service, "PT1H");
        }

        @Test
        void verifyOperationLongerValidityFromService() throws Throwable {
            val service = getSamlRegisteredServiceForTestShib(true, true);
            service.setValidityUntil("PT3H");
            checkResultAndValidity(service, "PT3H");
        }
    }

    @TestPropertySource(properties = "cas.authn.saml-idp.response.validity-until=PT2H")
    @Nested
    class LongerValidityTests extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val service = getSamlRegisteredServiceForTestShib(true, true);
            checkResultAndValidity(service, "PT2H");
        }
    }
}
