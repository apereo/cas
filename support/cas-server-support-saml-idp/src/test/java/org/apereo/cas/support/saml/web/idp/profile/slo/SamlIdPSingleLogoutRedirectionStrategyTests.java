package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

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
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.logout.send-logout-response=true",
    "cas.authn.saml-idp.logout.sign-logout-response=true"
})
public class SamlIdPSingleLogoutRedirectionStrategyTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("samlIdPLogoutResponseObjectBuilder")
    private SamlIdPLogoutResponseObjectBuilder samlIdPLogoutResponseObjectBuilder;

    @Autowired
    @Qualifier("samlIdPSingleLogoutRedirectionStrategy")
    private LogoutRedirectionStrategy samlIdPSingleLogoutRedirectionStrategy;

    @Test
    public void verifyOperationForPostBinding() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val registeredService = getSamlRegisteredServiceFor(false, false,
            false, "https://mockypost.io");
        WebUtils.putRegisteredService(request, registeredService);

        val logoutRequest = samlIdPLogoutResponseObjectBuilder.newLogoutRequest(
            UUID.randomUUID().toString(),
            ZonedDateTime.now(Clock.systemUTC()),
            "https://github.com/apereo/cas",
            samlIdPLogoutResponseObjectBuilder.newIssuer(registeredService.getServiceId()),
            UUID.randomUUID().toString(),
            samlIdPLogoutResponseObjectBuilder.getNameID(NameID.EMAIL, "cas@example.org"));
        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest)) {
            val encodedRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            WebUtils.putSingleLogoutRequest(request, encodedRequest);
        }

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        assertTrue(samlIdPSingleLogoutRedirectionStrategy.supports(context));
        assertNotNull(samlIdPSingleLogoutRedirectionStrategy.getName());

        samlIdPSingleLogoutRedirectionStrategy.handle(context);
        assertNull(WebUtils.getLogoutRedirectUrl(request, String.class));
    }

    @Test
    public void verifyOperationForRedirectBinding() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val registeredService = getSamlRegisteredServiceFor(false, false,
            false, "https://mocky.io");
        WebUtils.putRegisteredService(request, registeredService);

        val logoutRequest = samlIdPLogoutResponseObjectBuilder.newLogoutRequest(
            UUID.randomUUID().toString(),
            ZonedDateTime.now(Clock.systemUTC()),
            "https://github.com/apereo/cas",
            samlIdPLogoutResponseObjectBuilder.newIssuer(registeredService.getServiceId()),
            UUID.randomUUID().toString(),
            samlIdPLogoutResponseObjectBuilder.getNameID(NameID.EMAIL, "cas@example.org"));
        try (val writer = SamlUtils.transformSamlObject(openSamlConfigBean, logoutRequest)) {
            val encodedRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            WebUtils.putSingleLogoutRequest(request, encodedRequest);
        }

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        samlIdPSingleLogoutRedirectionStrategy.handle(context);
        assertNotNull(WebUtils.getLogoutRedirectUrl(request, String.class));
    }

    @Test
    public void verifyNoLogoutResponse() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val registeredService = getSamlRegisteredServiceFor(false, false,
            false, "https://mocky.io");
        registeredService.setLogoutResponseEnabled(false);
        WebUtils.putRegisteredService(request, registeredService);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertFalse(samlIdPSingleLogoutRedirectionStrategy.supports(context));
    }

    @Test
    public void verifyLogoutForNonSamlService() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        WebUtils.putRegisteredService(request, registeredService);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertFalse(samlIdPSingleLogoutRedirectionStrategy.supports(context));
    }
}
