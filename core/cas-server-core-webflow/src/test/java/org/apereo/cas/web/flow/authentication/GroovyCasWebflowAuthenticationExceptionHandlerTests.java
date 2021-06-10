package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyCasWebflowAuthenticationExceptionHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "cas.authn.errors.groovy.location=classpath:GroovyCasWebflowAuthenticationExceptionHandler.groovy"
})
@Tag("Groovy")
public class GroovyCasWebflowAuthenticationExceptionHandlerTests {
    @Autowired
    @Qualifier("groovyCasWebflowAuthenticationExceptionHandler")
    private CasWebflowExceptionHandler groovyCasWebflowAuthenticationExceptionHandler;

    @Test
    public void verifyOperation() {
        assertNotNull(groovyCasWebflowAuthenticationExceptionHandler);
        
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        assertTrue(groovyCasWebflowAuthenticationExceptionHandler.supports(new RuntimeException(), context));
        assertEquals("customEvent",
            groovyCasWebflowAuthenticationExceptionHandler.handle(new RuntimeException(), context).getId());
    }

}
