package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyScriptWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Groovy")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyScriptWebflowActionTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyShellScript() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val script = scriptFactory.fromScript("return new org.springframework.webflow.execution.Event(this, 'result')");
        val results = new GroovyScriptWebflowAction(script, applicationContext, casProperties);
        val result = results.execute(context);
        assertNotNull(result);
        assertEquals("result", result.getId());
    }

    @Test
    void verifyScript() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val groovyResource = new ClassPathResource("GroovyWebflowAction.groovy");
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val watchableScript = scriptFactory.fromResource(groovyResource);
        val results = new GroovyScriptWebflowAction(watchableScript, applicationContext, casProperties);
        val result = results.execute(context);
        assertNotNull(result);
        assertEquals("result", result.getId());
    }
}
