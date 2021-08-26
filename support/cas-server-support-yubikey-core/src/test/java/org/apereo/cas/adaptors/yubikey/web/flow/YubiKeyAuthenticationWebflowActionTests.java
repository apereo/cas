package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link YubiKeyAuthenticationWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
public class YubiKeyAuthenticationWebflowActionTests {
    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            request, new MockHttpServletResponse()));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val resolver = mock(CasWebflowEventResolver.class);
        when(resolver.resolve(any())).thenReturn(Set.of(new EventFactorySupport().success(this)));
        when(resolver.resolveSingle(any())).thenReturn(new EventFactorySupport().success(this));

        val action = new YubiKeyAuthenticationWebflowAction(resolver);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());

    }

}
