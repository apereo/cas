package org.apereo.cas.otp.web.flow;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.action.EventFactorySupport;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OneTimeTokenAuthenticationWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
class OneTimeTokenAuthenticationWebflowActionTests {
    @Test
    void verifyAction() throws Throwable {
        val resolver = mock(CasWebflowEventResolver.class);
        when(resolver.resolveSingle(any())).thenReturn(new EventFactorySupport()
            .event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS));
        val action = new OneTimeTokenAuthenticationWebflowAction(resolver);

        val context = MockRequestContext.create();
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }
}
