package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.scripting.GroovyShellScript;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyScriptWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Groovy")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyScriptWebflowActionTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyShellScript() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val script = new GroovyShellScript("return new org.springframework.webflow.execution.Event(this, 'result')");
        val results = new GroovyScriptWebflowAction(script, applicationContext, casProperties);
        val result = results.execute(context);
        assertNotNull(result);
        assertEquals("result", result.getId());
    }

    @Test
    void verifyScript() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val script = new WatchableGroovyScriptResource(new ClassPathResource("GroovyWebflowAction.groovy"));
        val results = new GroovyScriptWebflowAction(script, applicationContext, casProperties);
        val result = results.execute(context);
        assertNotNull(result);
        assertEquals("result", result.getId());
    }
}
