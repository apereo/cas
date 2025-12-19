package org.apereo.cas.web.flow.actions;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CheckWebAuthenticationRequestActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowAuthenticationActions")
class CheckWebAuthenticationRequestActionTests {

    @Test
    void verifyNoWeb() throws Throwable {
        val context = MockRequestContext.create();
        context.setContentType(MediaType.TEXT_HTML_VALUE);
        val action = new CheckWebAuthenticationRequestAction(MediaType.TEXT_HTML_VALUE);
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_NO, result.getId());
    }

    @Test
    void verifyYesWeb() throws Throwable {
        val context = MockRequestContext.create();
        context.setContentType(MediaType.TEXT_HTML_VALUE);
        val action = new CheckWebAuthenticationRequestAction(MediaType.APPLICATION_JSON_VALUE);
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_YES, result.getId());
    }

}
