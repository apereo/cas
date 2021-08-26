package org.apereo.cas.web.tomcat;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.X509TomcatServletFactoryInitialAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link X509TomcatServletWebServiceFactoryWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class X509TomcatServletWebServiceFactoryWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public X509TomcatServletWebServiceFactoryWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getX509().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            flow.getStartActionList().add(new X509TomcatServletFactoryInitialAction(casProperties));
        }
    }
}
