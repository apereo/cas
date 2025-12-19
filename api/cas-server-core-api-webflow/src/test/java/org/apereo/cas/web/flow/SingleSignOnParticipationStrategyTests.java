package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
class SingleSignOnParticipationStrategyTests {

    @Test
    void verifyOperation() throws Throwable {
        val input = mock(SingleSignOnParticipationStrategy.class);
        when(input.getOrder()).thenCallRealMethod();
        when(input.isCreateCookieOnRenewedAuthentication(any())).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, input.getOrder());

        val context = MockRequestContext.create();
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build();
        assertEquals(TriStateBoolean.UNDEFINED, input.isCreateCookieOnRenewedAuthentication(ssoRequest));
    }
}
