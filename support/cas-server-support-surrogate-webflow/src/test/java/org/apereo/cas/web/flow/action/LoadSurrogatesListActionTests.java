package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.web.flow.SurrogateWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.LinkedHashMap;
import java.util.Map;
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("loadSurrogatesListAction")
    private Action loadSurrogatesListAction;

    @Test
    public void verifyGetListView() {
        try {
            final var context = new MockRequestContext();
            final var request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

            WebUtils.putRequestSurrogateAuthentication(context, true);
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

            assertEquals(SurrogateWebflowConfigurer.VIEW_ID_SURROGATE_VIEW, loadSurrogatesListAction.execute(context).getId());
            assertNotNull(WebUtils.getSurrogateAuthenticationAccounts(context));
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifyAuthenticate() throws Exception {
        final var context = new MockRequestContext();
        WebUtils.putService(context, CoreAuthenticationTestUtils.getWebApplicationService());

        final Map attributes = new LinkedHashMap<>();
        attributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true);
        attributes.putAll(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());

        final var p = CoreAuthenticationTestUtils.getPrincipal("casuser", attributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(p), context);

        final var request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        final var creds = new SurrogateUsernamePasswordCredential();
        creds.setPassword("Mellon");
        creds.setUsername("casuser");
        creds.setSurrogateUsername("cassurrogate");
        WebUtils.putCredential(context, creds);

        final var builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(CoreAuthenticationTestUtils.getAuthentication()));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);
        
        WebUtils.putAuthenticationResultBuilder(builder, context);
        assertEquals("success", loadSurrogatesListAction.execute(context).getId());
    }
}
