package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Groovy")
@TestPropertySource(properties = "cas.webflow.groovy.location=classpath:/GroovyWebflow.groovy")
public class GroovyWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("groovyWebflowConfigurer")
    private CasWebflowConfigurer groovyWebflowConfigurer;

    @Test
    public void verifyOperation() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                groovyWebflowConfigurer.initialize();
            }
        });
    }

}
