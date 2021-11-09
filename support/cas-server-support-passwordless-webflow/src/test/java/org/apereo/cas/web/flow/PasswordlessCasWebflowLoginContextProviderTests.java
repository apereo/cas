package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordlessCasWebflowLoginContextProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowAuthenticationActions")
public class PasswordlessCasWebflowLoginContextProviderTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier("passwordlessCasWebflowLoginContextProvider")
    private CasWebflowLoginContextProvider passwordlessCasWebflowLoginContextProvider;

    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val account = new BasicIdentifiableCredential();
        account.setId(UUID.randomUUID().toString());
        WebUtils.putPasswordlessAuthenticationAccount(context, account);

        val results = passwordlessCasWebflowLoginContextProvider.getCandidateUsername(context);
        assertFalse(results.isEmpty());
        assertEquals(account.getId(), results.get());
    }

}
