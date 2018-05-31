package org.apereo.cas.web.flow.action;

import lombok.SneakyThrows;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy;
import org.apereo.cas.util.CollectionUtils;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateAuthorizationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SurrogateAuthorizationActionTests extends BaseSurrogateInitialAuthenticationActionTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("surrogateAuthorizationCheck")
    private Action surrogateAuthorizationCheck;

    @Test
    public void verifyAuthorized() {
        try {
            final var context = new MockRequestContext();
            WebUtils.putService(context, CoreAuthenticationTestUtils.getWebApplicationService());
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            final var registeredService = CoreAuthenticationTestUtils.getRegisteredService();
            final var strategy = new SurrogateRegisteredServiceAccessStrategy();
            when(registeredService.getAccessStrategy()).thenReturn(strategy);
            WebUtils.putRegisteredService(context, registeredService);
            final var request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals("success", surrogateAuthorizationCheck.execute(context).getId());
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @SneakyThrows
    public void verifyNotAuthorized() {
        final var context = new MockRequestContext();
        WebUtils.putService(context, CoreAuthenticationTestUtils.getWebApplicationService());

        final Map attributes = new LinkedHashMap<>();
        attributes.put(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, true);
        attributes.putAll(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());

        final var p = CoreAuthenticationTestUtils.getPrincipal("casuser", attributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(p), context);
        final var registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        final var strategy = new SurrogateRegisteredServiceAccessStrategy();
        strategy.setSurrogateEnabled(true);
        strategy.setSurrogateRequiredAttributes(CollectionUtils.wrap("surrogateAttribute", CollectionUtils.wrapSet("someValue")));
        when(registeredService.getAccessStrategy()).thenReturn(strategy);
        WebUtils.putRegisteredService(context, registeredService);
        final var request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        thrown.expect(PrincipalException.class);
        surrogateAuthorizationCheck.execute(context);
    }
}
