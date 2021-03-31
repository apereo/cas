package org.apereo.cas.adaptors.radius.authentication;

import org.apereo.cas.adaptors.radius.web.flow.BaseRadiusMultifactorAuthenticationTests;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import net.jradius.dictionary.Attr_State;
import net.jradius.packet.attribute.value.StringValue;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import javax.security.auth.login.FailedLoginException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RadiusTokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseRadiusMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.radius.server.protocol=PAP",
        "cas.authn.mfa.radius.client.shared-secret=testing123",
        "cas.authn.mfa.radius.client.inet-address=localhost"
    })
@Tag("Radius")
@EnabledOnOs(OS.LINUX)
public class RadiusTokenAuthenticationHandlerTests {

    @Autowired
    @Qualifier("radiusTokenAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Test
    public void verifyOperation() throws Exception {
        val c = new RadiusTokenCredential("Mellon");

        assertTrue(authenticationHandler.supports(c));
        assertTrue(authenticationHandler.supports(c.getClass()));

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(c));

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", 
            Map.of(Attr_State.NAME, List.of(new StringValue("value"))));
        val authn = CoreAuthenticationTestUtils.getAuthentication(principal);
        WebUtils.putAuthentication(authn, context);
        val result = authenticationHandler.authenticate(c);
        assertNotNull(result);
    }
}
