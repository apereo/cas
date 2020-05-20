package org.apereo.cas.otp.web.flow;

import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepositoryTests;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OneTimeTokenAuthenticationWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
@SpringBootTest(classes = BaseOneTimeTokenRepositoryTests.SharedTestConfiguration.class)
public class OneTimeTokenAuthenticationWebflowActionTests {
    @Test
    public void verifyAction() {
        val resolver = mock(CasWebflowEventResolver.class);
        when(resolver.resolveSingle(any())).thenReturn(new EventFactorySupport()
            .event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS));
        val action = new OneTimeTokenAuthenticationWebflowAction(resolver);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.doExecute(context).getId());
    }
}
