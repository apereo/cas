package org.apereo.cas.digest.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.digest.DigestCredential;
import org.apereo.cas.digest.config.DigestAuthenticationConfiguration;
import org.apereo.cas.digest.config.support.authentication.DigestAuthenticationComponentSerializationConfiguration;
import org.apereo.cas.digest.config.support.authentication.DigestAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.HttpConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DigestAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    DigestAuthenticationConfiguration.class,
    DigestAuthenticationEventExecutionPlanConfiguration.class,
    DigestAuthenticationComponentSerializationConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
}, properties = "cas.authn.digest.users.casuser=1234567890")
@Tag("Webflow")
public class DigestAuthenticationActionTests {
    @Autowired
    @Qualifier("digestAuthenticationAction")
    private Action digestAuthenticationAction;

    @Test
    public void verifyNoAuthn() throws Exception {
        assertNotNull(digestAuthenticationAction);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val result = digestAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void verifyBadDigest() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Digest 1234567890");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val result = digestAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }


    @Test
    public void verifyDigest() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();


        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        var result = digestAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

        val header = response.getHeader(HttpConstants.AUTHENTICATE_HEADER);
        assertNotNull(header);

        var digest = "username=\"casuser\",realm=\"CAS\",nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\""
            + ",uri=\"/login\",qop=auth,nc=00000001,cnonce=\"0a4f113b\","
            + "response=\"bad-client-response-digest\",opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        request.addHeader("Authorization", "Digest " + digest);
        result = digestAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

        digest = "username=\"casuser\",realm=\"CAS\",nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\""
            + ",uri=\"/login\",qop=auth,nc=00000001,cnonce=\"0a4f113b\","
            + "response=\"68a7c1eb3464e1c6c74adb230df614b8\",opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";
        request.removeHeader("Authorization");
        request.addHeader("Authorization", "Digest " + digest);
        result = digestAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertTrue(WebUtils.getCredential(context) instanceof DigestCredential);
    }

}
