package org.apereo.cas.web.flow;

import org.apereo.cas.util.model.TriStateBoolean;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class SingleSignOnParticipationStrategyTests {

    @Test
    public void verifyOperation() {
        val input = mock(SingleSignOnParticipationStrategy.class);
        when(input.getOrder()).thenCallRealMethod();
        when(input.isCreateCookieOnRenewedAuthentication(any())).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, input.getOrder());

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(new MockHttpServletRequest())
            .requestContext(new MockRequestContext())
            .build();
        assertEquals(TriStateBoolean.UNDEFINED, input.isCreateCookieOnRenewedAuthentication(ssoRequest));
    }
}
