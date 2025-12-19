package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.login.SetServiceUnauthorizedRedirectUrlAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SetServiceUnauthorizedRedirectUrlActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("WebflowServiceActions")
class SetServiceUnauthorizedRedirectUrlActionTests extends AbstractWebflowActionsTests {
    @BeforeEach
    void setup() throws Exception {
        val services = RegisteredServiceTestUtils.getRegisteredServicesForTests();
        getServicesManager().save(services.stream());
    }

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val action = new SetServiceUnauthorizedRedirectUrlAction(getServicesManager());

        val service = getWebApplicationServiceFactory().createService("https://github.com/apereo/cas");
        WebUtils.putRegisteredService(context, getServicesManager().findServiceBy(service));
        action.execute(context);
        assertNotNull(WebUtils.getUnauthorizedRedirectUrlFromFlowScope(context));
    }
}
