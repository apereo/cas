package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("WebflowActions")
class PrincipalFromRequestRemoteUserNonInteractiveCredentialsActionTests extends BaseNonInteractiveCredentialsActionTests {

    @Autowired
    @Qualifier("principalFromRemoteUserAction")
    private PrincipalFromRequestExtractorAction action;

    @Test
    void verifyRemoteUserExists() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getHttpServletRequest().setRemoteUser("test");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @Test
    void verifyRemoteUserDoesntExists() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());
    }
}
