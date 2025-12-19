package org.apereo.cas.web.flow.configurer;

import module java.base;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
class GroovyWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("groovyWebflowConfigurer")
    private CasWebflowConfigurer groovyWebflowConfigurer;

    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> groovyWebflowConfigurer.initialize());
    }

}
