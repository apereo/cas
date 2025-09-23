package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebflowActionBeanSupplierTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Webflow")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class WebflowActionBeanSupplierTests {
    @Nested
    @SpringBootTest(classes = RefreshAutoConfiguration.class,
        properties = "cas.webflow.groovy.actions.customActionId=unknown")
    class UnknownAction {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Autowired
        private CasConfigurationProperties casProperties;

        @Test
        void verifyUnknownScript() throws Throwable {
            val action = WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new StaticEventExecutionAction("pass"))
                .withId("customActionId")
                .build()
                .get();
            val context = MockRequestContext.create(applicationContext);
            assertEquals("pass", action.execute(context).getId());
        }
    }

    @Nested
    @SpringBootTest(classes = RefreshAutoConfiguration.class,
        properties = "cas.webflow.groovy.actions.customActionId=classpath:/GroovyWebflowAction.groovy")
    class GroovyAction {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Autowired
        private CasConfigurationProperties casProperties;

        @Test
        void verifyScript() throws Throwable {
            val action = WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new StaticEventExecutionAction("pass"))
                .withId("customActionId")
                .build()
                .get();

            val context = MockRequestContext.create(applicationContext);
            assertEquals("result", action.execute(context).getId());
        }
    }
}
