package org.apereo.cas.multitenancy;

import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockFilterChain;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantRoutingFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Web")
@SpringBootTest(classes = {
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    BaseMultitenancyTests.SharedTestConfiguration.class
},
    properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
@ExtendWith(CasTestExtension.class)
class TenantRoutingFilterTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("tenantRoutingFilter")
    private FilterRegistrationBean<TenantRoutingFilter> tenantRoutingFilter;

    @Test
    void verifyServerPrefix() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        requestContext.setServerName("sso.example.org");
        requestContext.setContextPath("/cas/tenants/b9584c42/login");
        requestContext.setServletPath("/login");
        requestContext.setClientInfo();
        val chain = new MockFilterChain();
        val response = requestContext.getHttpServletResponse();
        tenantRoutingFilter.getFilter().doFilter(requestContext.getHttpServletRequest(), response, chain);
        assertTrue(response.getForwardedUrl().startsWith("/tenants/b9584c42"));
    }

    @Test
    void verifyServerHost() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        requestContext.setServerName("sso.system.org");
        requestContext.setContextPath("/cas/tenants/hosted/login");
        requestContext.setServletPath("/login");
        requestContext.setClientInfo();
        val chain = new MockFilterChain();
        val response = requestContext.getHttpServletResponse();
        tenantRoutingFilter.getFilter().doFilter(requestContext.getHttpServletRequest(), response, chain);
        assertTrue(response.getForwardedUrl().startsWith("/tenants/hosted"));
    }
}
