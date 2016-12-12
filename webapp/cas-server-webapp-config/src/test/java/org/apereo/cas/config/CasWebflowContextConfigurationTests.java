package org.apereo.cas.config;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logging.config.CasLoggingConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link CasWebflowContextConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {CasApplicationContextConfiguration.class,
                CasFiltersConfiguration.class,
                CasMetricsConfiguration.class,
                CasPropertiesConfiguration.class,
                CasSecurityContextConfiguration.class,
                CasWebAppConfiguration.class,
                CasWebflowContextConfiguration.class,
                CasCoreWebflowConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasThemesConfiguration.class,
                CasLoggingConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasSupportActionsConfiguration.class,
                CasCoreUtilConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCookieConfiguration.class,
                CasCoreWebConfiguration.class,
                CasCoreValidationConfiguration.class,
                CasCoreConfiguration.class,
                CasCoreAuditConfiguration.class,
                CasPersonDirectoryConfiguration.class,
                ThymeleafAutoConfiguration.class,
                AopAutoConfiguration.class,
                RefreshAutoConfiguration.class
        })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@WebAppConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@TestPropertySource(properties = "spring.aop.proxy-target-class=true")
public class CasWebflowContextConfigurationTests {

    @Autowired
    @Qualifier("flowExecutorViaClientFlowExecution")
    private FlowExecutor flowExecutorViaClientFlowExecution;

    @Autowired
    @Qualifier("flowExecutorViaServerSessionBindingExecution")
    private FlowExecutor flowExecutorViaServerSessionBindingExecution;

    @Test
    public void verifyExecutorsAreBeans() {
        assertNotNull(flowExecutorViaClientFlowExecution);
        assertNotNull(flowExecutorViaServerSessionBindingExecution);
    }

    @Test
    public void verifyFlowExecutorByClient() {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        flowExecutorViaClientFlowExecution.launchExecution("login",
                new LocalAttributeMap<>(),
                new ServletExternalContext(sCtx, request, response));

    }
}
