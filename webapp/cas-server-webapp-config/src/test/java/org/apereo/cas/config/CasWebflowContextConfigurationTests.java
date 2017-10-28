package org.apereo.cas.config;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logging.config.CasLoggingConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;

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
                CasWebflowContextConfigurationTests.TestWebflowContextConfiguration.class,
                CasWebflowContextConfiguration.class,
                CasDefaultServiceTicketIdGeneratorsConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
                CasCoreWebflowConfiguration.class,
                CasCoreAuthenticationConfiguration.class, CasCoreServicesAuthenticationConfiguration.class,
                CasCoreAuthenticationPrincipalConfiguration.class,
                CasCoreAuthenticationPolicyConfiguration.class,
                CasCoreAuthenticationMetadataConfiguration.class,
                CasCoreAuthenticationSupportConfiguration.class,
                CasCoreAuthenticationHandlersConfiguration.class,
                CasCoreHttpConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreTicketCatalogConfiguration.class,
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
                CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
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
    public void verifyFlowExecutorByServerSession() {
        final RequestContext ctx = getMockRequestContext();
        final LocalAttributeMap map = new LocalAttributeMap<>();
        flowExecutorViaServerSessionBindingExecution.launchExecution("login", map, ctx.getExternalContext());

    }

    @Test
    public void verifyFlowExecutorByClient() {
        final RequestContext ctx = getMockRequestContext();
        final LocalAttributeMap map = new LocalAttributeMap<>();
        flowExecutorViaClientFlowExecution.launchExecution("login", map, ctx.getExternalContext());
    }

    private RequestContext getMockRequestContext() {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        return ctx;
    }

    @Configuration("testWebflowContextConfiguration")
    public static class TestWebflowContextConfiguration {

        private static final String TEST = "test";

        @Bean
        public Action testWebflowSerialization() {
            //CHECKSTYLE:OFF
            return new AbstractAction() {
                @Override
                protected Event doExecute(final RequestContext requestContext) {
                    requestContext.getFlowScope().put("test0", Collections.singleton(TEST));
                    requestContext.getFlowScope().put("test1", Collections.singletonList(TEST));
                    requestContext.getFlowScope().put("test2", Collections.singletonMap(TEST, TEST));
                    requestContext.getFlowScope().put("test3", Arrays.asList(TEST, TEST));
                    requestContext.getFlowScope().put("test4", new ConcurrentSkipListSet());
                    requestContext.getFlowScope().put("test5", Collections.unmodifiableList(Arrays.asList("test1")));
                    requestContext.getFlowScope().put("test6", Collections.unmodifiableSet(Collections.singleton(1)));
                    requestContext.getFlowScope().put("test7", Collections.unmodifiableMap(new HashMap<>()));
                    requestContext.getFlowScope().put("test8", Collections.emptyMap());
                    requestContext.getFlowScope().put("test9", new TreeMap<>());
                    requestContext.getFlowScope().put("test10", Collections.emptySet());
                    requestContext.getFlowScope().put("test11", Collections.emptyList());
                    return success();
                }
            };
            //CHECKSTYLE:ON
        }
    }
}
