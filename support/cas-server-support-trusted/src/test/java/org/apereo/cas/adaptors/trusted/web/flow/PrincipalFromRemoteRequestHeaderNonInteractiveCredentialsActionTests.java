package org.apereo.cas.adaptors.trusted.web.flow;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalFromRemoteRequestHeaderNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.authn.trusted.remote-principal-header=cas-header-name")
@Tag("WebflowActions")
class PrincipalFromRemoteRequestHeaderNonInteractiveCredentialsActionTests extends BaseNonInteractiveCredentialsActionTests {
    @Autowired
    @Qualifier("principalFromRemoteHeaderPrincipalAction")
    private PrincipalFromRequestExtractorAction action;

    @Test
    void verifyRemoteUserExists() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.addHeader("cas-header-name", "casuser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
        val credential = WebUtils.getCredential(context);
        assertEquals("casuser", credential.getId());
    }
}
