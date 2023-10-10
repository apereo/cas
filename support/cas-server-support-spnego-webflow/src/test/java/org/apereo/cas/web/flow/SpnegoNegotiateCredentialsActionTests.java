package org.apereo.cas.web.flow;

import org.apereo.cas.support.spnego.util.SpnegoConstants;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.HttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpnegoNegotiateCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Spnego")
class SpnegoNegotiateCredentialsActionTests extends AbstractSpnegoTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.addHeader("User-Agent", "MSIE");

        negociateSpnegoAction.execute(context);
        assertNotNull(context.getHttpServletResponse().getHeader(SpnegoConstants.HEADER_AUTHENTICATE));
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, context.getHttpServletResponse().getStatus());
    }

    @Test
    void verifyEmptyAgent() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, negociateSpnegoAction.execute(context).getId());

        context.addHeader("User-Agent", "UnknownBrowser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, negociateSpnegoAction.execute(context).getId());
    }

    @Test
    void verifyBadAuthzHeader() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.addHeader("User-Agent", "MSIE");
        context.addHeader(SpnegoConstants.HEADER_AUTHORIZATION, SpnegoConstants.NEGOTIATE + " XYZ");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, negociateSpnegoAction.execute(context).getId());
    }
}
