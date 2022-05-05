package org.apereo.cas.web.flow.client;

import org.apereo.cas.web.flow.AbstractSpnegoTests;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class BaseLdapSpnegoKnownClientSystemsFilterActionTests extends AbstractSpnegoTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SPNEGO_CLIENT_LDAP)
    protected Action ldapSpnegoClientAction;

    @Test
    public void ensureLdapAttributeShouldDoSpnego() throws Exception {
        val ctx = new MockRequestContext();
        val req = new MockHttpServletRequest();
        req.setRemoteAddr("localhost");
        val extCtx = new ServletExternalContext(
            new MockServletContext(), req,
            new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);
        val ev = ldapSpnegoClientAction.execute(ctx);
        assertEquals(new EventFactorySupport().yes(this).getId(), ev.getId());
    }
}
