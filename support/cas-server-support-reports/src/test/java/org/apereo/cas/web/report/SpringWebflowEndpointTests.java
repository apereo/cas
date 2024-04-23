package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.VariableValueFactory;
import org.springframework.webflow.engine.ViewVariable;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.ActionExecutingViewFactory;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.execution.AnnotatedAction;
import org.springframework.webflow.execution.ViewFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SpringWebflowEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("ActuatorEndpoint")
@Import(SpringWebflowEndpointTests.SpringWebflowEndpointTestConfiguration.class)
@TestPropertySource(properties = "management.endpoint.springWebflow.enabled=true")
class SpringWebflowEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("springWebflowEndpoint")
    private SpringWebflowEndpoint springWebflowEndpoint;

    @Test
    void verifyOperation() throws Throwable {
        val login = springWebflowEndpoint.getReport("login", null);
        assertNotNull(login);

        val logout = springWebflowEndpoint.getReport("logout", null);
        assertNotNull(logout);

        val all = springWebflowEndpoint.getReport(StringUtils.EMPTY, null);
        assertNotNull(all);
    }

    @TestConfiguration(value = "SpringWebflowEndpointTestConfiguration", proxyBeanMethods = false)
    static class SpringWebflowEndpointTestConfiguration {

        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        private FlowDefinitionRegistry loginFlowDefinitionRegistry;

        @Autowired
        private FlowBuilderServices flowBuilderServices;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Bean
        public CasWebflowExecutionPlanConfigurer testWebflowExecutionPlanConfigurer(
            @Qualifier("testCasWebflowConfigurer")
            final CasWebflowConfigurer testCasWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(testCasWebflowConfigurer);
        }

        @Bean
        public CasWebflowConfigurer testCasWebflowConfigurer() {
            return new AbstractCasWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties) {
                @Override
                protected void doInitialize() {
                    val flow = getLoginFlow();

                    if (flow != null) {
                        val viewFactory = createExternalRedirectViewFactory("github.com");
                        val externalViewState = createViewState(flow, "exampleViewExternal", viewFactory);
                        externalViewState.setCaption("View Caption");

                        val valueFactory = mock(VariableValueFactory.class);
                        when(valueFactory.createInitialValue(any())).thenReturn(new Object());
                        externalViewState.addVariable(new ViewVariable("var1", valueFactory));

                        createViewState(flow, "exampleViewUnknownFactory", mock(ViewFactory.class));
                        val executingViewFactory = mock(ActionExecutingViewFactory.class);
                        when(executingViewFactory.getAction()).thenReturn(createEvaluateAction("example"));
                        createViewState(flow, "exampleViewOtherFactory", executingViewFactory);

                        val subflow = createSubflowState(flow, "subflowStateId", "logout");
                        subflow.getEntryActionList().add(new AnnotatedAction(createSetAction("annotated", "true")));
                        subflow.getEntryActionList().add(new AnnotatedAction(createEvaluateAction("flowScope.annotated=true")));

                        flow.getEndActionList().add(createSetAction("endOfFlow", "true"));
                        val transition = new Transition(new DefaultTransitionCriteria(
                            new LiteralExpression("input-global")), new DefaultTargetStateResolver("target"));
                        flow.getGlobalTransitionSet().add(transition);
                    }
                }
            };
        }
    }
}
