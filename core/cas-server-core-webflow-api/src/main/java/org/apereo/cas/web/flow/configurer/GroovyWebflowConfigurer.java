package org.apereo.cas.web.flow.configurer;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
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
    public GroovyWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                   final FlowDefinitionRegistry flowDefinitionRegistry,
                                   final ConfigurableApplicationContext applicationContext,
                                   final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    public void doInitialize() {
        FunctionUtils.doAndHandle(_ -> {
            val resource = casProperties.getWebflow().getGroovy().getLocation();
            if (resource != null) {
                val args = new Object[]{this, applicationContext, LOGGER};
                LOGGER.debug("Executing Groovy script [{}] to auto-configure the webflow context", resource);
                val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
                try (val script = scriptFactory.fromResource(resource)) {
                    script.execute(args, Object.class, true);
                }
            }
        });
    }
}
