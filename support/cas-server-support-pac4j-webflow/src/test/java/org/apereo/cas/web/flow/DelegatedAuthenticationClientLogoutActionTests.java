package org.apereo.cas.web.flow;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationClientLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes =
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Webflow")
public class DelegatedAuthenticationClientLogoutActionTests {
    @Autowired
    @Qualifier("delegatedAuthenticationClientLogoutAction")
    private Action delegatedAuthenticationClientLogoutAction;

    @Test
    public void verifyOperationWithProfile() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val profile = new CommonProfile();
        profile.setId("casuser");
        profile.setClientName("CasClient");
        request.setAttribute(Pac4jConstants.USER_PROFILES,
            CollectionUtils.wrapLinkedHashMap(profile.getClientName(), profile));
        val result = delegatedAuthenticationClientLogoutAction.execute(context);
        assertNull(result);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
    }

    @Test
    public void verifyOperationWithNoProfile() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val result = delegatedAuthenticationClientLogoutAction.execute(context);
        assertNull(result);
        assertNotEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatus());
    }
}
