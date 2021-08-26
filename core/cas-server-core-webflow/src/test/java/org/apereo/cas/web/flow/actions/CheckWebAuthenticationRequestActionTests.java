package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CheckWebAuthenticationRequestActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowAuthenticationActions")
public class CheckWebAuthenticationRequestActionTests {

    @Test
    public void verifyNoWeb() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setContentType(MediaType.TEXT_HTML_VALUE);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val action = new CheckWebAuthenticationRequestAction(MediaType.TEXT_HTML_VALUE);
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_NO, result.getId());
    }

    @Test
    public void verifyYesWeb() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setContentType(MediaType.TEXT_HTML_VALUE);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val action = new CheckWebAuthenticationRequestAction(MediaType.APPLICATION_JSON_VALUE);
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_YES, result.getId());
    }

}
