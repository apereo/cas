package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import module java.base;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlAuthNStatementBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAMLResponse")
class SamlProfileSamlAuthNStatementBuilderTests {

    class AbstractValidityUntilSamlProfileSamlAuthNStatementBuilderTests extends BaseSamlIdPConfigurationTests {
        protected static final int ONE_HOUR = 60;
        private static final int ONE_MINUTE = 1;

        @Autowired
        @Qualifier("samlProfileSamlAuthNStatementBuilder")
        protected SamlProfileObjectBuilder<AuthnStatement> samlProfileSamlAuthNStatementBuilder;

        protected void checkResultAndValidity(final SamlRegisteredService service, final int expectedValidity) throws Exception {
            val adaptor = SamlRegisteredServiceMetadataAdaptor.get(
                    samlRegisteredServiceCachingMetadataResolver,
                    service, service.getServiceId()).get();

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
            val now = ZonedDateTime.now(ZoneOffset.UTC);
            assertTrue(sessionNotOnOrAfter.isAfter(now.plusMinutes(expectedValidity - ONE_MINUTE).toInstant()));
            assertTrue(sessionNotOnOrAfter.isBefore(now.plusMinutes(expectedValidity + ONE_MINUTE).toInstant()));
        }
    }

    @Nested
    class DefaultValidityUntilSamlProfileSamlAuthNStatementBuilderTests
            extends AbstractValidityUntilSamlProfileSamlAuthNStatementBuilderTests {
        @Test
        void verifyOperationDefaultSettings() throws Throwable {
            val service = getSamlRegisteredServiceForTestShib(true, true);
            checkResultAndValidity(service, ONE_HOUR);
        }

        @Test
        void verifyOperationLongerValidityFromService() throws Throwable {
            val service = getSamlRegisteredServiceForTestShib(true, true);
            service.setValidityUntil(3 * ONE_HOUR * 60);
            checkResultAndValidity(service, 3 * ONE_HOUR);
        }
    }

    @SpringBootTest(
        classes = BaseSamlIdPConfigurationTests.SharedTestConfiguration.class,
        properties = {
            "server.port=8383",
            "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS",
            "management.endpoints.web.exposure.include=*",
            "management.endpoints.access.default=UNRESTRICTED",

            "cas.webflow.crypto.encryption.key=qLhvLuaobvfzMmbo9U_bYA",
            "cas.webflow.crypto.signing.key=oZeAR5pEXsolruu4OQYsQKxf-FCvFzSsKlsVaKmfIl6pNzoPm6zPW94NRS1af7vT-0bb3DpPBeksvBXjloEsiA",
            "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
            "cas.authn.saml-idp.metadata.http.metadata-backup-location=file://${java.io.tmpdir}/metadata-backups",
            "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata-${#randomNumber8}",
            "cas.authn.saml-idp.response.validity-until=PT2H"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @Nested
    class LongerValidityByConfigurationSamlProfileSamlAuthNStatementBuilderTests
            extends AbstractValidityUntilSamlProfileSamlAuthNStatementBuilderTests {
        @Test
        void verifyOperation() throws Throwable {
            val service = getSamlRegisteredServiceForTestShib(true, true);
            checkResultAndValidity(service, 2 * ONE_HOUR);
        }
    }
}
