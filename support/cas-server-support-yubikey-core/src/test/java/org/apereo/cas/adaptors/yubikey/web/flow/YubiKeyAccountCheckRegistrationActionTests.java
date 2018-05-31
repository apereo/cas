package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.AcceptAllYubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.DenyAllYubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.ClosedYubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.registry.OpenYubiKeyAccountRegistry;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link YubiKeyAccountCheckRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class YubiKeyAccountCheckRegistrationActionTests {
    @Test
    public void verifyActionSuccess() throws Exception {
        final var context = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        final var action =
            new YubiKeyAccountCheckRegistrationAction(new OpenYubiKeyAccountRegistry(new AcceptAllYubiKeyAccountValidator()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @Test
    public void verifyActionRegister() throws Exception {
        final var context = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        final var authentication = CoreAuthenticationTestUtils.getAuthentication();
        WebUtils.putAuthentication(authentication, context);
        final var registry = new ClosedYubiKeyAccountRegistry(new DenyAllYubiKeyAccountValidator());
        final var action = new YubiKeyAccountCheckRegistrationAction(registry);
        assertEquals("register", action.execute(context).getId());

    }
}
