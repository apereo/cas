package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockRequestContext;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.validation.Validator;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.ViewFactoryCreator;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.validation.BeanValidationHintResolver;
import java.io.Serial;
import java.io.Serializable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasMvcViewFactoryCreatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Webflow")
class CasMvcViewFactoryCreatorTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("viewFactoryCreator")
    private ViewFactoryCreator viewFactoryCreator;

    @Autowired
    @Qualifier("flowBuilderServices")
    private FlowBuilderServices flowBuilderServices;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        context.setRequestAttribute("_eventId", "continue")
            .setRequestAttribute("name", "Bob")
            .setRequestAttribute("token", "ABC")
            .setRequestAttribute("accountId", "1000");

        val model = new Model();
        context.getCurrentState().getAttributes().put("model", new StaticExpression(model));

        val binderConfiguration = new BinderConfiguration();
        binderConfiguration.addBinding(new BinderConfiguration.Binding("name", null, true));
        binderConfiguration.addBinding(new BinderConfiguration.Binding("token", null, true));
        binderConfiguration.addBinding(new BinderConfiguration.Binding("accountId", null, true));
        
        val factory = viewFactoryCreator.createViewFactory(new LiteralExpression("login"),
            flowBuilderServices.getExpressionParser(), flowBuilderServices.getConversionService(),
            binderConfiguration, mock(Validator.class), new BeanValidationHintResolver());
        val view = factory.getView(context);
        assertNotNull(view);
        assertTrue(view.userEventQueued());

        view.processUserEvent();
        assertEquals("Bob", model.getName());
        assertEquals("ABC", model.getToken());
        assertEquals(1000, model.getAccountId());
        
    }

    @Data
    static class Model implements Serializable {
        @Serial
        private static final long serialVersionUID = -6707100969970651189L;
        private String name;
        private String token;
        private long accountId;
    }
}
