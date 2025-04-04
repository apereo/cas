package org.apereo.cas.multitenancy;

import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.decorator.WebflowDecorator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantWebflowDecoratorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Webflow")
@SpringBootTest(classes = {
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    BaseMultitenancyTests.SharedTestConfiguration.class
},
    properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
@ExtendWith(CasTestExtension.class)
class TenantWebflowDecoratorTests {
    @Autowired
    @Qualifier("casMultitenancyWebflowDecorator")
    private WebflowDecorator casMultitenancyWebflowDecorator;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val requestContext = MockRequestContext.create(applicationContext);
        requestContext.setContextPath("/tenants/b9584c42/login");
        casMultitenancyWebflowDecorator.decorate(requestContext);
        assertTrue(requestContext.getFlowScope().contains("tenantUserInterfacePolicy"));
    }
}
