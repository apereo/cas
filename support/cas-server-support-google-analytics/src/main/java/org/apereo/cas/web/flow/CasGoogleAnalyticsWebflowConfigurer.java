package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CasGoogleAnalyticsWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class CasGoogleAnalyticsWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String ATTRIBUTE_FLOWSCOPE_GOOGLE_ANALYTICS_TRACKING_ID = "googleAnalyticsTrackingId";

    public CasGoogleAnalyticsWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                               final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                               final ApplicationContext applicationContext,
                                               final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            injectGoogleAnalyticsTrackingId(flow);
        }
        
        val logoutFlow = getLogoutFlow();
        if (logoutFlow != null) {
            injectGoogleAnalyticsTrackingId(logoutFlow);
            val logoutSetup = getState(logoutFlow, CasWebflowConstants.STATE_ID_LOGOUT_VIEW, EndState.class);
            logoutSetup.getEntryActionList().add(insertGoogleAnalyticsTrackingIdAction());
        }
    }

    private void injectGoogleAnalyticsTrackingId(final Flow flow) {
        flow.getStartActionList().add(insertGoogleAnalyticsTrackingIdAction());
    }

    private Action insertGoogleAnalyticsTrackingIdAction() {
        return requestContext -> {
            putGoogleAnalyticsTrackingIdIntoFlowScope(requestContext, casProperties.getGoogleAnalytics().getGoogleAnalyticsTrackingId());
            return null;
        };
    }

    /**
     * Put tracking id into flow scope.
     *
     * @param context the context
     * @param value   the value
     */
    private static void putGoogleAnalyticsTrackingIdIntoFlowScope(final RequestContext context, final String value) {
        if (StringUtils.isBlank(value)) {
            context.getFlowScope().remove(ATTRIBUTE_FLOWSCOPE_GOOGLE_ANALYTICS_TRACKING_ID);
        } else {
            context.getFlowScope().put(ATTRIBUTE_FLOWSCOPE_GOOGLE_ANALYTICS_TRACKING_ID, value);
        }
    }
}
