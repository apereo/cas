package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StoreUserIdForAuthenticationActionTests}.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@Tag("Webflow")
public class StoreUserIdForAuthenticationActionTests extends BaseMultiphaseAuthenticationActionTests {
    @Autowired
    @Qualifier("storeUserIdForAuthenticationAction")
    private Action storeUserIdForAuthenticationAction;

    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        context.setCurrentEvent(new Event(this, "processing"));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
                    request, new MockHttpServletResponse()));
        request.addParameter("username", "casuser");
        assertEquals("success", storeUserIdForAuthenticationAction.execute(context).getId());
    }
}

