package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationClientRetryActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("WebflowAuthenticationActions")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DelegatedAuthenticationClientRetryActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY)
    private Action retryAction;

    @Test
    public void verifyOperationWithRedirect() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        WebUtils.putDelegatedAuthenticationClientName(context, "SAML2Client");
        val result = retryAction.execute(context);
        assertNull(result);
        assertNotNull(response.getHeaderValue("Location"));
        assertEquals(HttpStatus.FOUND.value(), response.getStatus());
    }
}
