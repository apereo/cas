package org.apereo.cas.web.flow.executor;

import lombok.val;
import org.cryptacular.bean.AEADBlockCipherBean;
import org.cryptacular.bean.KeyStoreFactoryBean;
import org.cryptacular.generator.sp80038d.RBGNonce;
import org.cryptacular.io.ClassPathResource;
import org.cryptacular.spec.AEADBlockCipherSpec;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.test.annotation.DirtiesContext;
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
 * @author Misagh Moayyed
 * @since 6.1
 */
@SpringBootTest(classes = {
    ClientFlowExecutionRepositoryTests.WebflowTestConfiguration.class,
    RefreshAutoConfiguration.class
})
@DirtiesContext
@Tag("Webflow")
public class ClientFlowExecutionRepositoryTests {

    @Autowired
    @Qualifier("flowExecutor")
    private FlowExecutor flowExecutor;

    @Test
    public void verifyBadKey() {
        val factory = new ClientFlowExecutionRepository(mock(FlowExecutionFactory.class), mock(FlowDefinitionLocator.class), mock(Transcoder.class));
        factory.removeFlowExecutionSnapshot(mock(FlowExecution.class));
        factory.removeAllFlowExecutionSnapshots(mock(FlowExecution.class));
        assertThrows(ClientFlowExecutionRepositoryException.class, () -> factory.getKey(mock(FlowExecution.class)));
        assertThrows(IllegalArgumentException.class, () -> factory.getFlowExecution(mock(FlowExecutionKey.class)));
        val key = mock(ClientFlowExecutionKey.class);
        when(key.getData()).thenThrow(IllegalArgumentException.class);
        assertThrows(ClientFlowExecutionRepositoryException.class, () -> factory.getFlowExecution(key));

    }
    @Test
    public void verifyLaunchAndResumeFlow() {
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

    @TestConfiguration("WebflowTestConfiguration")
    @Lazy(false)
    public static class WebflowTestConfiguration {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Bean
        public FlowExecutor flowExecutor() {
            val impl = new FlowExecutionImplFactory();
            val repo = getFlowExecutionRepository(impl);
            impl.setExecutionKeyFactory(repo);
            return new FlowExecutorImpl(flowRegistry(), flowExecutionFactory(), repo);
        }

        @Bean
        public FlowDefinitionRegistry flowRegistry() {
            val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilder());
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
        public FlowExecutionFactory flowExecutionFactory() {
            val impl = new FlowExecutionImplFactory();
            val repo = getFlowExecutionRepository(impl);
            impl.setExecutionKeyFactory(repo);
            return impl;
        }

        private ClientFlowExecutionRepository getFlowExecutionRepository(final FlowExecutionFactory impl) {
            val repo = new ClientFlowExecutionRepository();
            repo.setFlowExecutionFactory(impl);
            repo.setFlowDefinitionLocator(flowRegistry());
            repo.setTranscoder(transcoder());
            return repo;
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
