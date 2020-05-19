package org.apereo.cas.configuration;

import org.apereo.cas.util.CasVersion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.bind.UnboundConfigurationPropertiesException;
import org.springframework.boot.context.properties.bind.handler.IgnoreTopLevelConverterNotFoundBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.UnboundElementsSourceFilter;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is {@link CasConfigurationPropertiesValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasConfigurationPropertiesValidator {
    private final ConfigurableApplicationContext applicationContext;

    /**
     * Validate the cas configuration properties
     * from property sources found in the application
     * context and report back results..
     *
     * @return the list
     */
    public List<String> validate() {
        val validationResults = new ArrayList<String>(0);
        validateCasConfiguration(validationResults);
        if (validationResults.isEmpty()) {
            LOGGER.info("Application context has validated CAS property sources and configuration successfully.");
        } else {
            var message = String.join("\n", validationResults);
            message += "\n\nListed settings above are no longer recognized by CAS " + CasVersion.getVersion() + ". They may have been renamed, removed, or relocated "
                + "to a new address in the CAS configuration schema. CAS will ignore such settings and will proceed with its normal initialization sequence. "
                + "Please consult the CAS documentation to review and adjust each setting to find an alternative or remove the "
                + "definition. Failure to do so puts the stability of the CAS server deployment in danger and complicates future upgrades.\n";
            LOGGER.error(message);
        }
        return validationResults;
    }

    private void validateCasConfiguration(final List<String> validationResults) {
        try {
            validateConfiguration(CasConfigurationProperties.class, validationResults);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }

    private void validateConfiguration(final Class clazz, final List<String> validationResults) {
        val beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext.getBeanFactory(), clazz);
        beans.values().forEach(bean -> {
            val configBean = ConfigurationPropertiesBean.get(this.applicationContext, bean, UUID.randomUUID().toString());
            val target = configBean.asBindTarget();
            val annotation = configBean.getAnnotation();

            val handler = new NoUnboundElementsBindHandler(
                new IgnoreTopLevelConverterNotFoundBindHandler(),
                new UnboundElementsSourceFilter());


            val configBinder = new Binder(ConfigurationPropertySources.from(applicationContext.getEnvironment().getPropertySources()),
                new PropertySourcesPlaceholdersResolver(applicationContext.getEnvironment().getPropertySources()),
                applicationContext.getEnvironment().getConversionService(),
                null, null,
                null);
            try {
                configBinder.bind(annotation.prefix(), target, handler);
            } catch (final BindException e) {
                var message = "\n".concat(e.getMessage()).concat("\n");
                if (e.getCause() != null) {
                    val cause = (UnboundConfigurationPropertiesException) e.getCause();
                    if (cause != null) {
                        message += cause.getUnboundProperties()
                            .stream()
                            .map(property -> String.format("%n\t%s = %s (Origin: %s)", property.getName(), property.getValue(), property.getOrigin()))
                            .collect(Collectors.joining("\n"));
                    }
                }
                validationResults.add(message);
            }
        });
    }
}
