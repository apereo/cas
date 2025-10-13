package org.apereo.cas.web.view;

import org.apereo.cas.BaseThymeleafTests;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.FlowExecutionException;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasThymeleafConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseThymeleafTests.SharedTestConfiguration.class,
    properties = {
        "spring.web.resources.chain.strategy.content.enabled=true",
        "cas.view.rest.url=http://localhost:8182",
        "cas.view.rest.maximum-retry-attempts=0",
        "cas.view.template-prefixes=classpath:templates,file:/templates"
    })
@Tag("Web")
@ExtendWith(CasTestExtension.class)
class CasThymeleafConfigurationTests {
    @Autowired
    @Qualifier("chainingTemplateViewResolver")
    private AbstractTemplateResolver chainingTemplateViewResolver;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
    private FlowDefinitionRegistry flowDefinitionRegistry;

    @Autowired
    @Qualifier(CasWebflowExecutionPlan.BEAN_NAME)
    private CasWebflowExecutionPlan casWebflowExecutionPlan;

    @BeforeEach
    void setup() {
        casWebflowExecutionPlan.execute();
    }


    /**
     * Make sure there are 7 template view resolvers.
     * Two for each template prefix, one for rest url,and one for a theme folder under templates in the
     * classpath and one for templates in thymeleaf/templates.
     */
    @Test
    void verifyOperation() {
        assertNotNull(chainingTemplateViewResolver);
        assertEquals(7, ((ChainingTemplateViewResolver) chainingTemplateViewResolver).getResolvers().size());
        assertNotNull(chainingTemplateViewResolver.resolveTemplate(mock(IEngineConfiguration.class), null, "testTemplate", new HashMap<>()));
    }

    @Test
    void verifyWebflowConfigError() throws Throwable {
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        val stopState = (EndState) flow.getState(CasWebflowConstants.STATE_ID_VIEW_WEBFLOW_CONFIG_ERROR);
        val context = MockRequestContext.create(applicationContext);
        context.setActiveFlow(flow);
        context.getFlashScope().put(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION, new PreventedException("Prevented"));
        context.getFlashScope().put("flowExecutionException",
            new FlowExecutionException(CasWebflowConfigurer.FLOW_ID_LOGIN, "failureState", "Failed"));
        assertDoesNotThrow(() -> stopState.enter(context));
    }
}
