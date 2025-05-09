package org.apereo.cas.web.flow.executor;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutorResolver;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.config.FlowBuilderServicesBuilder;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.expression.spel.WebFlowSpringELExpressionParser;
import org.springframework.webflow.test.CasMockViewFactoryCreator;
import org.springframework.webflow.test.MockExternalContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link ClientFlowExecutionRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1
 */
@SpringBootTestAutoConfigurations
@Tag("Webflow")
@ExtendWith(CasTestExtension.class)
class ClientFlowExecutionRepositoryTests {

    @Nested
    @Import(WebflowTestConfiguration.class)
    @TestPropertySource(properties = "cas.webflow.session.pin-to-session=true")
    class SessionPinningTests extends BaseWebflowConfigurerTests {
        @Autowired
        @Qualifier("flowExecutor")
        private FlowExecutor flowExecutor;

        @Test
        void verifyLaunchAndResumeFlow() {
            val request1 = new MockHttpServletRequest();
            request1.setRemoteAddr("223.456.789.000");
            request1.setLocalAddr("123.456.789.000");
            request1.addHeader(HttpHeaders.USER_AGENT, "test");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request1));

            val context = new MockExternalContext();
            context.setNativeRequest(request1);
            context.setNativeResponse(new MockHttpServletResponse());

            val launchResult = flowExecutor.launchExecution("test-flow", new LocalAttributeMap<>(), context);
            assertNotNull(launchResult.getPausedKey());
            context.setEventId("submit");

            val request2 = new MockHttpServletRequest();
            request2.setRemoteAddr("223.456.789.000");
            request2.setLocalAddr("123.456.789.000");
            request2.addHeader(HttpHeaders.USER_AGENT, "test-another");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request2));
            context.setNativeRequest(request2);
            assertThrows(ClientFlowExecutionRepositoryException.class, () -> flowExecutor.resumeExecution(launchResult.getPausedKey(), context));
        }
    }

    @Nested
    @Import(WebflowTestConfiguration.class)
    class DefaultTests extends BaseWebflowConfigurerTests {
        @Autowired
        @Qualifier("flowExecutor")
        private FlowExecutor flowExecutor;

        @Test
        void verifyBadKey() {
            val factory = new ClientFlowExecutionRepository(mock(FlowExecutionFactory.class),
                mock(FlowDefinitionLocator.class), CipherExecutorResolver.with(CipherExecutor.noOp()), casProperties.getWebflow());
            factory.removeFlowExecutionSnapshot(mock(FlowExecution.class));
            factory.removeAllFlowExecutionSnapshots(mock(FlowExecution.class));
            assertThrows(ClientFlowExecutionRepositoryException.class, () -> factory.getKey(mock(FlowExecution.class)));
            assertThrows(IllegalArgumentException.class, () -> factory.getFlowExecution(mock(FlowExecutionKey.class)));
            val key = mock(ClientFlowExecutionKey.class);
            when(key.getData()).thenThrow(IllegalArgumentException.class);
            assertThrows(ClientFlowExecutionRepositoryException.class, () -> factory.getFlowExecution(key));
        }

        @Test
        void verifyLaunchAndResumeFlow() {
            val context = new MockExternalContext();
            context.setNativeRequest(new MockHttpServletRequest());
            context.setNativeResponse(new MockHttpServletResponse());

            val launchResult = flowExecutor.launchExecution("test-flow", new LocalAttributeMap<>(), context);
            assertNotNull(launchResult.getPausedKey());
            val key = ClientFlowExecutionKey.parse(launchResult.getPausedKey());
            assertEquals(key.toString(), launchResult.getPausedKey());
            context.setEventId("submit");
            context.getRequestMap().put("vegan", "0");
            val resumeResult = flowExecutor.resumeExecution(launchResult.getPausedKey(), context);
            assertNotNull(resumeResult.getOutcome());
            assertEquals("lasagnaDinner", resumeResult.getOutcome().getId());
        }
    }


    @Nested
    @Import(WebflowTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
    class MultitenancyTests extends BaseWebflowConfigurerTests {
        @Autowired
        @Qualifier("flowExecutor")
        private FlowExecutor flowExecutor;

        @Test
        void verifyLaunchAndResumeFlow() {
            val context = new MockExternalContext();
            val request = new MockHttpServletRequest();
            request.setRemoteAddr("223.456.789.100");
            request.setLocalAddr("223.456.789.100");
            request.addHeader(HttpHeaders.USER_AGENT, "test");
            request.setContextPath("/tenants/webflow/login");

            ClientInfoHolder.setClientInfo(ClientInfo.from(request));

            context.setNativeRequest(request);
            context.setNativeResponse(new MockHttpServletResponse());

            val launchResult = flowExecutor.launchExecution("test-flow", new LocalAttributeMap<>(), context);
            assertNotNull(launchResult.getPausedKey());
            val key = ClientFlowExecutionKey.parse(launchResult.getPausedKey());
            assertEquals(key.toString(), launchResult.getPausedKey());
        }
    }


    @TestConfiguration(value = "WebflowTestConfiguration", proxyBeanMethods = false)
    static class WebflowTestConfiguration {
        @Bean
        public FlowExecutor flowExecutor(
            @Qualifier("clientFlowExecutionRepository")
            final ClientFlowExecutionRepository clientFlowExecutionRepository) {
            return new FlowExecutorImpl(clientFlowExecutionRepository.getFlowDefinitionLocator(),
                clientFlowExecutionRepository.getFlowExecutionFactory(), clientFlowExecutionRepository);
        }

        @Bean
        public FlowDefinitionRegistry flowRegistry(final ConfigurableApplicationContext applicationContext) {
            val flowServicesBuilder = new FlowBuilderServicesBuilder();
            flowServicesBuilder.setViewFactoryCreator(new CasMockViewFactoryCreator());
            flowServicesBuilder.setExpressionParser(new WebFlowSpringELExpressionParser(new SpelExpressionParser()));
            val flowBuilder = flowServicesBuilder.build();

            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilder);
            builder.setBasePath("classpath:");
            builder.addFlowLocationPattern("/test/*-flow.xml");
            return builder.build();
        }

        @Bean
        public Transcoder transcoder() {
            val cipher = new WebflowConversationStateCipherExecutor("AES", 512, 16);
            return new EncryptedTranscoder(cipher);
        }

        @Bean
        public ClientFlowExecutionRepository clientFlowExecutionRepository(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(CipherExecutor.BEAN_NAME_WEBFLOW_CIPHER_EXECUTOR)
            final CipherExecutor webflowCipherExecutor,
            final CasConfigurationProperties casProperties,
            @Qualifier("flowRegistry")
            final FlowDefinitionRegistry flowRegistry) {

            val factory = new WebflowExecutorFactory(casProperties.getWebflow(), flowRegistry,
                webflowCipherExecutor, new FlowExecutionListener[0],
                new DefaultFlowUrlHandler(), tenantExtractor);
            val flowExecutor = (WebflowExecutorFactory.CasFlowExecutorImpl) factory.build();
            return (ClientFlowExecutionRepository) flowExecutor.getFlowExecutionRepository();
        }
    }

}
