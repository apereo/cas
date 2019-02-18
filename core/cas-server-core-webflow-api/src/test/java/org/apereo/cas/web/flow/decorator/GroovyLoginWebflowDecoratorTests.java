package org.apereo.cas.web.flow.decorator;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyLoginWebflowDecoratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class GroovyLoginWebflowDecoratorTests {
    @Test
    public void verifyOperation() {
        val groovy = new GroovyLoginWebflowDecorator(new ClassPathResource("GroovyLoginWebflowDecorator.groovy"));
        val requestContext = new MockRequestContext();
        groovy.decorate(requestContext, mock(ApplicationContext.class));
        assertTrue(requestContext.getFlowScope().contains("decoration"));
    }
}
