package org.apereo.cas.web.flow;

import org.apereo.cas.support.spnego.MockJcifsAuthentication;
import org.apereo.cas.support.spnego.util.SpnegoConstants;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;

import jcifs.spnego.Authentication;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpnegoCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Import(SpnegoCredentialsActionTests.SpnegoAuthenticationTestConfiguration.class)
@Tag("Spnego")
public class SpnegoCredentialsActionTests extends AbstractSpnegoTests {
    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addHeader(SpnegoConstants.HEADER_AUTHORIZATION, SpnegoConstants.NEGOTIATE + ' ' + EncodingUtils.encodeBase64("credential"));
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        spnegoAction.execute(context);
        assertNotNull(response.getHeader(SpnegoConstants.HEADER_AUTHENTICATE));
    }

    @Test
    public void verifyNoAuthzHeader() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, spnegoAction.execute(context).getId());
    }

    @Test
    public void verifyBadAuthzHeader() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addHeader(SpnegoConstants.HEADER_AUTHORIZATION, "XYZ");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, spnegoAction.execute(context).getId());
    }


    @TestConfiguration("SpnegoAuthenticationTestConfiguration")
    @Lazy(false)
    public static class SpnegoAuthenticationTestConfiguration {
        @Bean
        public List<Authentication> spnegoAuthentications() {
            return CollectionUtils.wrapList(new MockJcifsAuthentication());
        }
    }
}
