package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.login.SetServiceUnauthorizedRedirectUrlAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
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
    
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://github.com/apereo/cas");
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        registeredService.setAccessStrategy(accessStrategy);
        getServicesManager().save(registeredService);
        
        val action = new SetServiceUnauthorizedRedirectUrlAction(getServicesManager());
        val service = getWebApplicationServiceFactory().createService(registeredService.getServiceId());
        WebUtils.putRegisteredService(context, getServicesManager().findServiceBy(service));
        action.execute(context);
        assertNotNull(WebUtils.getUnauthorizedRedirectUrlFromFlowScope(context));
    }
}
