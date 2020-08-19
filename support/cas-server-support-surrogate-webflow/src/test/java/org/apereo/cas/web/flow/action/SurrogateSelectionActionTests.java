package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateSelectionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
public class SurrogateSelectionActionTests extends BaseSurrogateInitialAuthenticationActionTests {

    @Autowired
    @Qualifier("selectSurrogateAction")
    private Action selectSurrogateAction;

    @Test
    public void verifyFails() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(mock(ServletExternalContext.class));
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, selectSurrogateAction.execute(context).getId());
    }

    @Test
    public void verifyNoCredentialFound() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(SurrogateSelectionAction.PARAMETER_NAME_SURROGATE_TARGET, "cassurrogate");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, selectSurrogateAction.execute(context).getId());
        val c = WebUtils.getCredential(context);
        assertNull(c);
    }

    @Test
    public void verifyCredentialFound() throws Exception {
        val context = new MockRequestContext();
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val request = new MockHttpServletRequest();

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(CoreAuthenticationTestUtils.getAuthentication("casuser")));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        request.addParameter(SurrogateSelectionAction.PARAMETER_NAME_SURROGATE_TARGET, "cassurrogate");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, selectSurrogateAction.execute(context).getId());
    }
}
