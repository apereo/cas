package org.apereo.cas.adaptors.trusted.web.flow;

import module java.base;
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
 * @since 3.0.5
 */
@Tag("WebflowActions")
class PrincipalFromRequestUserPrincipalNonInteractiveCredentialsActionTests extends BaseNonInteractiveCredentialsActionTests {

    @Autowired
    @Qualifier("principalFromRemoteUserPrincipalAction")
    private PrincipalFromRequestExtractorAction action;

    @Test
    void verifyRemoteUserExists() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getHttpServletRequest().setUserPrincipal(() -> "test");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
    }

    @Test
    void verifyRemoteUserDoesntExists() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, this.action.execute(context).getId());
    }

}
