package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SingleSignOnParticipationRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Simple")
public class SingleSignOnParticipationRequestTests {
    @Test
    public void verifyOperation() {
        val httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        val requestContext = new MockRequestContext();
        val response = new MockHttpServletResponse();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(), httpServletRequest, response));
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(httpServletRequest)
            .requestContext(requestContext)
            .attributes(new HashMap<>(Map.of("hello", "world")))
            .build()
            .attribute("testkey", List.of("testvalue"));
        assertTrue(ssoRequest.containsAttribute("hello"));
        assertNotNull(ssoRequest.getAttributeValue("hello", String.class));
        assertTrue(ssoRequest.getRequestContext().isPresent());
        assertTrue(ssoRequest.getHttpServletRequest().isPresent());
        assertTrue(ssoRequest.isRequestingRenewAuthentication());
    }
}
