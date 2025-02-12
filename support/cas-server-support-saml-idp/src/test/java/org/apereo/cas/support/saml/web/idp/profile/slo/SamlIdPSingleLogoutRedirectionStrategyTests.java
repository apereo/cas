package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.util.Saml20ObjectBuilder;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPSingleLogoutRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAMLLogout")
class SamlIdPSingleLogoutRedirectionStrategyTests {

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.logout.send-logout-response=true",
        "cas.authn.saml-idp.logout.sign-logout-response=true",
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata6414"
    })
    class SignResponsesGlobally extends BaseSamlIdPConfigurationTests {
        @Autowired
        @Qualifier("samlIdPLogoutResponseObjectBuilder")
        private Saml20ObjectBuilder samlIdPLogoutResponseObjectBuilder;

        @Autowired
        @Qualifier("samlIdPSingleLogoutRedirectionStrategy")
        private LogoutRedirectionStrategy samlIdPSingleLogoutRedirectionStrategy;

        @Test
        void verifyOperationForPostBinding() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val registeredService = getSamlRegisteredServiceFor(false, false,
                false, "https://mockypost.io");
            WebUtils.putRegisteredService(context.getHttpServletRequest(), registeredService);

            val logoutRequest = samlIdPLogoutResponseObjectBuilder.newLogoutRequest(
                UUID.randomUUID().toString(),
                ZonedDateTime.now(Clock.systemUTC()),
                "https://github.com/apereo/cas",
                samlIdPLogoutResponseObjectBuilder.newIssuer(registeredService.getServiceId()),
                UUID.randomUUID().toString(),
                samlIdPLogoutResponseObjectBuilder.newNameID(NameIDType.EMAIL, "cas@example.org"));
            try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest)) {
                val encodedRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
                WebUtils.putSingleLogoutRequest(context.getHttpServletRequest(), encodedRequest);
            }

            assertTrue(samlIdPSingleLogoutRedirectionStrategy.supports(context.getHttpServletRequest(), context.getHttpServletResponse()));
            assertNotNull(samlIdPSingleLogoutRedirectionStrategy.getName());

            samlIdPSingleLogoutRedirectionStrategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertNull(WebUtils.getLogoutRedirectUrl(context.getHttpServletRequest(), String.class));
        }

        @Test
        void verifyOperationForRedirectBindingByService() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val registeredService = getSamlRegisteredServiceFor(false, false,
                false, "https://mocky.io");
            registeredService.setLogoutResponseBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
            WebUtils.putRegisteredService(context.getHttpServletRequest(), registeredService);

            val logoutRequest = samlIdPLogoutResponseObjectBuilder.newLogoutRequest(
                UUID.randomUUID().toString(),
                ZonedDateTime.now(Clock.systemUTC()),
                "https://github.com/apereo/cas",
                samlIdPLogoutResponseObjectBuilder.newIssuer(registeredService.getServiceId()),
                UUID.randomUUID().toString(),
                samlIdPLogoutResponseObjectBuilder.newNameID(NameIDType.EMAIL, "cas@example.org"));
            try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest)) {
                val encodedRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
                WebUtils.putSingleLogoutRequest(context.getHttpServletRequest(), encodedRequest);
            }
            samlIdPSingleLogoutRedirectionStrategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertNotNull(WebUtils.getLogoutRedirectUrl(context.getHttpServletRequest(), String.class));
        }

        @Test
        void verifyOperationForRedirectBinding() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val registeredService = getSamlRegisteredServiceFor(false, false,
                false, "https://mocky.io");
            WebUtils.putRegisteredService(context.getHttpServletRequest(), registeredService);

            val logoutRequest = samlIdPLogoutResponseObjectBuilder.newLogoutRequest(
                UUID.randomUUID().toString(),
                ZonedDateTime.now(Clock.systemUTC()),
                "https://github.com/apereo/cas",
                samlIdPLogoutResponseObjectBuilder.newIssuer(registeredService.getServiceId()),
                UUID.randomUUID().toString(),
                samlIdPLogoutResponseObjectBuilder.newNameID(NameIDType.EMAIL, "cas@example.org"));
            try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest)) {
                val encodedRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
                WebUtils.putSingleLogoutRequest(context.getHttpServletRequest(), encodedRequest);
            }
            samlIdPSingleLogoutRedirectionStrategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertNotNull(WebUtils.getLogoutRedirectUrl(context.getHttpServletRequest(), String.class));
        }

        @Test
        void verifyNoLogoutResponse() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val registeredService = getSamlRegisteredServiceFor(false, false,
                false, "https://mocky.io");
            registeredService.setLogoutResponseEnabled(false);
            WebUtils.putRegisteredService(context.getHttpServletRequest(), registeredService);
            assertFalse(samlIdPSingleLogoutRedirectionStrategy.supports(context.getHttpServletRequest(), context.getHttpServletResponse()));
        }

        @Test
        void verifyLogoutForNonSamlService() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            WebUtils.putRegisteredService(context.getHttpServletRequest(), registeredService);
            assertFalse(samlIdPSingleLogoutRedirectionStrategy.supports(context.getHttpServletRequest(), context.getHttpServletResponse()));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata956",
        "cas.authn.saml-idp.logout.send-logout-response=true"
    })
    class SignResponsesServices extends BaseSamlIdPConfigurationTests {
        @Autowired
        @Qualifier("samlIdPLogoutResponseObjectBuilder")
        private Saml20ObjectBuilder samlIdPLogoutResponseObjectBuilder;

        @Autowired
        @Qualifier("samlIdPSingleLogoutRedirectionStrategy")
        private LogoutRedirectionStrategy samlIdPSingleLogoutRedirectionStrategy;

        @Test
        void verifyOperationForPostBinding() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val registeredService = getSamlRegisteredServiceFor(false, false,
                false, "https://mockypost.io");
            registeredService.setSignLogoutResponse(TriStateBoolean.TRUE);
            WebUtils.putRegisteredService(context.getHttpServletRequest(), registeredService);

            val logoutRequest = samlIdPLogoutResponseObjectBuilder.newLogoutRequest(
                UUID.randomUUID().toString(),
                ZonedDateTime.now(Clock.systemUTC()),
                "https://github.com/apereo/cas",
                samlIdPLogoutResponseObjectBuilder.newIssuer(registeredService.getServiceId()),
                UUID.randomUUID().toString(),
                samlIdPLogoutResponseObjectBuilder.newNameID(NameIDType.EMAIL, "cas@example.org"));
            try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest)) {
                val encodedRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
                WebUtils.putSingleLogoutRequest(context.getHttpServletRequest(), encodedRequest);
            }

            assertTrue(samlIdPSingleLogoutRedirectionStrategy.supports(context.getHttpServletRequest(), context.getHttpServletResponse()));
            assertNotNull(samlIdPSingleLogoutRedirectionStrategy.getName());
            samlIdPSingleLogoutRedirectionStrategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertNull(WebUtils.getLogoutRedirectUrl(context.getHttpServletRequest(), String.class));
        }
    }
}
