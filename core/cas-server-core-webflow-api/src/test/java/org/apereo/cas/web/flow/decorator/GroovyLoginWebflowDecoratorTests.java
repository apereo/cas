package org.apereo.cas.web.flow.decorator;

import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyLoginWebflowDecoratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Groovy")
class GroovyLoginWebflowDecoratorTests {
    @Test
    void verifyOperation() throws Throwable {
        val groovy = new GroovyLoginWebflowDecorator(new ClassPathResource("GroovyLoginWebflowDecorator.groovy"));
        val applicationContext = mock(ApplicationContext.class);
        val requestContext = MockRequestContext.create(applicationContext);
        groovy.decorate(requestContext);
        assertTrue(requestContext.getFlowScope().contains("decoration"));
    }
}
