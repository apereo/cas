package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.action.EventFactorySupport;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link YubiKeyAuthenticationWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
class YubiKeyAuthenticationWebflowActionTests {
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        val resolver = mock(CasWebflowEventResolver.class);
        when(resolver.resolve(any())).thenReturn(Set.of(new EventFactorySupport().success(this)));
        when(resolver.resolveSingle(any())).thenReturn(new EventFactorySupport().success(this));

        val action = new YubiKeyAuthenticationWebflowAction(resolver, mock(TenantExtractor.class));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }
}
