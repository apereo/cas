package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.validation.Validator;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.ViewFactoryCreator;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.validation.BeanValidationHintResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasMvcViewFactoryCreatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Webflow")
public class CasMvcViewFactoryCreatorTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("viewFactoryCreator")
    private ViewFactoryCreator viewFactoryCreator;

    @Autowired
    @Qualifier("flowBuilderServices")
    private FlowBuilderServices flowBuilderServices;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        context.getHttpServletRequest().setAttribute("_eventId", "continue");
        val factory = viewFactoryCreator.createViewFactory(new LiteralExpression("login"),
            flowBuilderServices.getExpressionParser(), flowBuilderServices.getConversionService(),
            new BinderConfiguration(), mock(Validator.class), new BeanValidationHintResolver());
        val view = factory.getView(context);
        assertNotNull(view);
        assertTrue(view.userEventQueued());
    }
}
