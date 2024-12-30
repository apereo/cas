package org.apereo.cas.web.flow.executor;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.flow.WebflowProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.cryptacular.bean.AEADBlockCipherBean;
import org.cryptacular.bean.KeyStoreFactoryBean;
import org.cryptacular.generator.sp80038d.RBGNonce;
import org.cryptacular.io.ClassPathResource;
import org.cryptacular.spec.AEADBlockCipherSpec;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.config.FlowBuilderServicesBuilder;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionLocator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.execution.FlowExecution;
import org.springframework.webflow.execution.FlowExecutionFactory;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
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
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        ClientFlowExecutionRepositoryTests.WebflowTestConfiguration.class
    }, properties = "cas.webflow.session.pin-to-session=true")
    class SessionPinningTests {
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

            val launchResult = flowExecutor.launchExecution("test-flow", new LocalAttributeMap<>(), new MockExternalContext());
            assertNotNull(launchResult.getPausedKey());
            val context = new MockExternalContext();
            context.setEventId("submit");
            
            val request2 = new MockHttpServletRequest();
            request2.setRemoteAddr("223.456.789.000");
            request2.setLocalAddr("123.456.789.000");
            request2.addHeader(HttpHeaders.USER_AGENT, "test-another");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request2));
            assertThrows(ClientFlowExecutionRepositoryException.class, () -> flowExecutor.resumeExecution(launchResult.getPausedKey(), context));
        }
    }
    
    @Nested
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        ClientFlowExecutionRepositoryTests.WebflowTestConfiguration.class
    })
    class DefaultTests {
        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        @Qualifier("flowExecutor")
        private FlowExecutor flowExecutor;

        @Test
        void verifyBadKey() {
            val factory = new ClientFlowExecutionRepository(mock(FlowExecutionFactory.class),
                mock(FlowDefinitionLocator.class), mock(Transcoder.class), casProperties.getWebflow());
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
            assertNotNull(flowExecutor);
            val launchResult = flowExecutor.launchExecution("test-flow", new LocalAttributeMap<>(), new MockExternalContext());
            assertNotNull(launchResult.getPausedKey());
            try {
                val key = ClientFlowExecutionKey.parse(launchResult.getPausedKey());
                assertEquals(key.toString(), launchResult.getPausedKey());
            } catch (final BadlyFormattedFlowExecutionKeyException e) {
                fail(() -> "Error parsing flow execution key: " + e.getMessage());
            }
            val context = new MockExternalContext();
            context.setEventId("submit");
            context.getRequestMap().put("vegan", "0");
            val resumeResult = flowExecutor.resumeExecution(launchResult.getPausedKey(), context);
            assertNotNull(resumeResult.getOutcome());
            assertEquals("lasagnaDinner", resumeResult.getOutcome().getId());
        }
    }

    @TestConfiguration(value = "WebflowTestConfiguration", proxyBeanMethods = false)
    static class WebflowTestConfiguration {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        private static ClientFlowExecutionRepository getFlowExecutionRepository(
            final Transcoder transcoder,
            final FlowDefinitionRegistry flowRegistry,
            final FlowExecutionFactory impl,
            final WebflowProperties webflowProperties) {
            val repo = new ClientFlowExecutionRepository();
            repo.setFlowExecutionFactory(impl);
            repo.setFlowDefinitionLocator(flowRegistry);
            repo.setTranscoder(transcoder);
            repo.setWebflowProperties(webflowProperties);
            return repo;
        }

        @Bean
        public FlowExecutor flowExecutor(
            final CasConfigurationProperties casProperties,
            @Qualifier("transcoder") final Transcoder transcoder,
            @Qualifier("flowExecutionFactory") final FlowExecutionFactory flowExecutionFactory,
            @Qualifier("flowRegistry") final FlowDefinitionRegistry flowRegistry) {
            val impl = new FlowExecutionImplFactory();
            val repo = getFlowExecutionRepository(transcoder, flowRegistry, impl, casProperties.getWebflow());
            impl.setExecutionKeyFactory(repo);
            return new FlowExecutorImpl(flowRegistry, flowExecutionFactory, repo);
        }

        @Bean
        public FlowDefinitionRegistry flowRegistry(
            @Qualifier("flowBuilder") final FlowBuilderServices flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilder);
            builder.setBasePath("classpath:");
            builder.addFlowLocationPattern("/test/*-flow.xml");
            return builder.build();
        }

        @Bean
        public FlowBuilderServices flowBuilder() {
            val builder = new FlowBuilderServicesBuilder();
            builder.setViewFactoryCreator(new CasMockViewFactoryCreator());
            builder.setExpressionParser(new WebFlowSpringELExpressionParser(new SpelExpressionParser()));
            return builder.build();
        }

        @Bean
        public FlowExecutionFactory flowExecutionFactory(
            final CasConfigurationProperties casProperties,
            @Qualifier("transcoder") final Transcoder transcoder,
            @Qualifier("flowRegistry") final FlowDefinitionRegistry flowRegistry) {
            val impl = new FlowExecutionImplFactory();
            val repo = getFlowExecutionRepository(transcoder, flowRegistry, impl, casProperties.getWebflow());
            impl.setExecutionKeyFactory(repo);
            return impl;
        }

        @Bean
        public Transcoder transcoder() {
            val keystoreFactory = new KeyStoreFactoryBean();
            keystoreFactory.setType("JCEKS");
            keystoreFactory.setPassword("changeit");
            keystoreFactory.setResource(new ClassPathResource("test-keystore.jceks"));

            val cipher = new AEADBlockCipherBean();
            cipher.setKeyAlias("aes128");
            cipher.setKeyPassword("changeit");
            cipher.setKeyStore(keystoreFactory.newInstance());
            cipher.setBlockCipherSpec(new AEADBlockCipherSpec("AES", "GCM"));
            cipher.setNonce(new RBGNonce());

            return new EncryptedTranscoder(cipher);
        }
    }
}
