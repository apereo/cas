package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.SurrogateWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.LinkedHashMap;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LoadSurrogatesListActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class LoadSurrogatesListActionTests extends BaseSurrogateInitialAuthenticationActionTests {

    @Autowired
    @Qualifier("loadSurrogatesListAction")
    private Action loadSurrogatesListAction;

    @Test
    public void verifyGetListView() {
        try {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

            WebUtils.putRequestSurrogateAuthentication(context, true);
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

            assertEquals(SurrogateWebflowConfigurer.TRANSITION_ID_SURROGATE_VIEW, loadSurrogatesListAction.execute(context).getId());
            assertNotNull(WebUtils.getSurrogateAuthenticationAccounts(context));
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifyAuthenticate() throws Exception {
        val context = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());

        val attributes = new LinkedHashMap<String, Object>();
        attributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true);
        attributes.putAll(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());

        val p = CoreAuthenticationTestUtils.getPrincipal("casuser", attributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(p), context);

        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val creds = new SurrogateUsernamePasswordCredential();
        creds.setPassword("Mellon");
        creds.setUsername("casuser");
        creds.setSurrogateUsername("cassurrogate");
        WebUtils.putCredential(context, creds);

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(CoreAuthenticationTestUtils.getAuthentication()));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, loadSurrogatesListAction.execute(context).getId());
    }

    @Test
    public void verifySkipAuthenticate() throws Exception {
        val context = new MockRequestContext();
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putRequestSurrogateAuthentication(context, Boolean.TRUE);

        val attributes = new LinkedHashMap<String, Object>();
        attributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true);
        attributes.putAll(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());

        val p = CoreAuthenticationTestUtils.getPrincipal("someuser", attributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(p), context);

        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val creds = new SurrogateUsernamePasswordCredential();
        creds.setPassword("Mellon");
        creds.setUsername("someuser");
        creds.setSurrogateUsername("others");
        WebUtils.putCredential(context, creds);

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(CoreAuthenticationTestUtils.getAuthentication()));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);

        WebUtils.putAuthenticationResultBuilder(builder, context);
        assertEquals(SurrogateWebflowConfigurer.TRANSITION_ID_SKIP_SURROGATE, loadSurrogatesListAction.execute(context).getId());
    }
}
