package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class WebflowActionBeanSupplierTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyUnknownScript() throws Throwable {
        val properties = new CasConfigurationProperties();
        properties.getWebflow().getGroovy().getActions().put("customActionId", "unknown");

        val action = WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(properties)
            .withAction(() -> new StaticEventExecutionAction("pass"))
            .withId("customActionId")
            .build()
            .get();

        val context = MockRequestContext.create();
        assertEquals("pass", action.execute(context).getId());
    }

    @Test
    void verifyScript() throws Throwable {
        val properties = new CasConfigurationProperties();
        properties.getWebflow().getGroovy().getActions().put("customActionId", "classpath:/GroovyWebflowAction.groovy");

        val action = WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(properties)
            .withAction(() -> new StaticEventExecutionAction("pass"))
            .withId("customActionId")
            .build()
            .get();

        val context = MockRequestContext.create();
        assertEquals("result", action.execute(context).getId());
    }
}
