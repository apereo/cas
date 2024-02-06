package org.apereo.cas.web.flow.client;

import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.AbstractSpnegoTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Action;
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
    void ensureLdapAttributeShouldDoSpnego() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        context.setRemoteAddr("localhost");
        val ev = ldapSpnegoClientAction.execute(context);
        assertEquals(new EventFactorySupport().yes(this).getId(), ev.getId());
    }
}
