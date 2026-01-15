package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@TestPropertySource(properties = "cas.sso.services.allow-missing-service-parameter=false")
@Tag("WebflowActions")
class InitializeLoginActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION)
    private Action action;

    @Test
    void disableFlowIfNoService() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setMethod(HttpMethod.POST);
        assertThrows(NoSuchFlowExecutionException.class, () -> this.action.execute(context));
    }
}
