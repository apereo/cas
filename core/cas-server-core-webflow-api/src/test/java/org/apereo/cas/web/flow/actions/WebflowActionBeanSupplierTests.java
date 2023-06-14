package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

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
    public void verifyUnknownScript() throws Exception {
        val properties = new CasConfigurationProperties();
        properties.getWebflow().getGroovy().getActions().put("customActionId", "unknown");

        val action = WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(properties)
            .withAction(() -> new StaticEventExecutionAction("pass"))
            .withId("customActionId")
            .build()
            .get();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertEquals("pass", action.execute(context).getId());
    }

    @Test
    public void verifyScript() throws Exception {
        val properties = new CasConfigurationProperties();
        properties.getWebflow().getGroovy().getActions().put("customActionId", "classpath:/GroovyWebflowAction.groovy");

        val action = WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(properties)
            .withAction(() -> new StaticEventExecutionAction("pass"))
            .withId("customActionId")
            .build()
            .get();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertEquals("result", action.execute(context).getId());
    }
}
