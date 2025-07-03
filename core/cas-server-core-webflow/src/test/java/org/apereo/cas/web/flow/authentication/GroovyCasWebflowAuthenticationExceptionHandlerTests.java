package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyCasWebflowAuthenticationExceptionHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
@TestPropertySource(properties = "cas.authn.errors.groovy.location=classpath:GroovyCasWebflowAuthenticationExceptionHandler.groovy")
@Tag("Groovy")
class GroovyCasWebflowAuthenticationExceptionHandlerTests {
    @Autowired
    @Qualifier("groovyCasWebflowAuthenticationExceptionHandler")
    private CasWebflowExceptionHandler groovyCasWebflowAuthenticationExceptionHandler;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(groovyCasWebflowAuthenticationExceptionHandler);

        val context = MockRequestContext.create(applicationContext);

        assertTrue(groovyCasWebflowAuthenticationExceptionHandler.supports(new RuntimeException(), context));
        assertEquals("customEvent",
            groovyCasWebflowAuthenticationExceptionHandler.handle(new RuntimeException(), context).getId());
    }

}
