package org.apereo.cas.web.flow.actions;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationClientRetryActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
class DelegatedAuthenticationClientRetryActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY)
    private Action retryAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperationWithRedirect() throws Throwable {
        val context = MockRequestContext.create(applicationContext).withUserAgent().setClientInfo();
        DelegationWebflowUtils.putDelegatedAuthenticationClientName(context, "CasClient");
        val result = retryAction.execute(context);
        assertNull(result);
        assertNotNull(context.getHttpServletResponse().getHeaderValue("Location"));
        assertEquals(HttpStatus.FOUND.value(), context.getHttpServletResponse().getStatus());
    }
}
