package org.apereo.cas;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasFiltersConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPropertiesConfiguration;
import org.apereo.cas.config.CasWebAppConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logging.config.CasLoggingConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.test.MockRequestContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.Assert.*;

/**
 * This is {@link BaseCasWebflowSessionContextConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootTest(classes = {
    CasThemesConfiguration.class,
    CasFiltersConfiguration.class,
    CasPropertiesConfiguration.class,
    CasWebAppConfiguration.class,
    CasWebflowServerSessionContextConfigurationTests.TestWebflowContextConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
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
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@TestPropertySource(properties = "spring.aop.proxy-target-class=true")
public abstract class BaseCasWebflowSessionContextConfigurationTests {
    @Test
    public void verifyExecutorsAreBeans() {
        assertNotNull(getFlowExecutor());
    }

    @Test
    public void verifyFlowExecutorByClient() {
        val ctx = getMockRequestContext();
        val map = new LocalAttributeMap<Object>();
        getFlowExecutor().launchExecution("login", map, ctx.getExternalContext());
    }

    @Test
    public void verifyCasPropertiesAreAvailableInView() {
        val ctx = getMockRequestContext();
        val map = new LocalAttributeMap<Object>();
        getFlowExecutor().launchExecution("login", map, ctx.getExternalContext());
        assertResponseWrittenEquals("classpath:expected/end.html", ctx);
    }

    @SneakyThrows(IOException.class)
    protected void assertResponseWrittenEquals(final String response, final MockRequestContext context) {
        val nativeResponse = (MockHttpServletResponse) context.getExternalContext().getNativeResponse();

        assertEquals(
            IOUtils.toString(new InputStreamReader(ResourceUtils.getResourceFrom(response).getInputStream(), StandardCharsets.UTF_8)),
            nativeResponse.getContentAsString()
        );
    }

    private static MockRequestContext getMockRequestContext() {
        val ctx = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        return ctx;
    }

    public abstract FlowExecutor getFlowExecutor();

    /**
     * The type Test webflow context configuration.
     */
    @TestConfiguration("testWebflowContextConfiguration")
    public static class TestWebflowContextConfiguration {
        private static final String TEST = "test";

        @Autowired
        @Qualifier("principalElectionStrategy")
        private ObjectProvider<PrincipalElectionStrategy> principalElectionStrategy;

        @Bean
        public Action testWebflowSerialization() {
            //CHECKSTYLE:OFF
            return new AbstractAction() {
                @Override
                protected Event doExecute(final RequestContext requestContext) {
                    val flowScope = requestContext.getFlowScope();
                    flowScope.put("test", TEST);
                    flowScope.put("test0", Collections.singleton(TEST));
                    flowScope.put("test1", Collections.singletonList(TEST));
                    flowScope.put("test2", Collections.singletonMap(TEST, TEST));
                    flowScope.put("test3", Arrays.asList(TEST, TEST));
                    flowScope.put("test4", new ConcurrentSkipListSet());
                    flowScope.put("test5", List.of("test1"));
                    flowScope.put("test6", Collections.unmodifiableSet(Collections.singleton(1)));
                    flowScope.put("test7", Collections.unmodifiableMap(new HashMap<>()));
                    flowScope.put("test8", Collections.emptyMap());
                    flowScope.put("test9", new TreeMap<>());
                    flowScope.put("test10", Collections.emptySet());
                    flowScope.put("test11", Collections.emptyList());

                    val service = new SimpleWebApplicationServiceImpl();
                    service.setId(CoreAuthenticationTestUtils.CONST_TEST_URL);
                    service.setOriginalUrl(CoreAuthenticationTestUtils.CONST_TEST_URL);
                    service.setArtifactId(null);

                    val authentication = CoreAuthenticationTestUtils.getAuthentication();
                    val authenticationResultBuilder = new DefaultAuthenticationResultBuilder();
                    val principal = CoreAuthenticationTestUtils.getPrincipal();
                    authenticationResultBuilder.collect(authentication);
                    authenticationResultBuilder.collect(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
                    val authenticationResult = authenticationResultBuilder.build(principalElectionStrategy.getIfAvailable(), service);

                    WebUtils.putAuthenticationResultBuilder(authenticationResultBuilder, requestContext);
                    WebUtils.putAuthenticationResult(authenticationResult, requestContext);
                    WebUtils.putPrincipal(requestContext, principal);

                    return success();
                }
            };
            //CHECKSTYLE:ON
        }
    }
}
