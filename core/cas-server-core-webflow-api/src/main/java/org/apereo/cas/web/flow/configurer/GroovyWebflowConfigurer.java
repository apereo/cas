package org.apereo.cas.web.flow.configurer;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ScriptingUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link GroovyWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyWebflowConfigurer extends AbstractCasWebflowConfigurer {


    public GroovyWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                   final ApplicationContext applicationContext, final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    public void doInitialize() {
        final Resource script = casProperties.getWebflow().getGroovy().getLocation();
        if (script != null) {
            final Object[] args = new Object[]{this, applicationContext, LOGGER};
            LOGGER.debug("Executing Groovy script [{}] to auto-configure the webflow context", script);
            ScriptingUtils.executeGroovyScript(script, args, Object.class);
        }
    }
}
