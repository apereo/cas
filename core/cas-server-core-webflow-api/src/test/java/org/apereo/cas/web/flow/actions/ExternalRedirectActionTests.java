package org.apereo.cas.web.flow.actions;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.ExternalRedirectAction;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.http.HttpHeaders;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ExternalRedirectActionTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("WebflowActions")
class ExternalRedirectActionTests {
    @Test
    void verifyExternal() throws Exception {
        val action = new ExternalRedirectAction(new LiteralExpression("https://apereo.github.io"));
        val context = MockRequestContext.create();
        val results = action.execute(context);
        assertEquals("https://apereo.github.io", context.getHttpServletResponse().getHeader(HttpHeaders.LOCATION));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, results.getId());
    }

    @Test
    void verifyDirect() throws Exception {
        val action = new ExternalRedirectAction(new LiteralExpression("custom://apereo.github.io"));
        val context = MockRequestContext.create();
        val results = action.execute(context);
        assertEquals("custom://apereo.github.io", context.getHttpServletResponse().getHeader(HttpHeaders.LOCATION));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, results.getId());
    }
}
