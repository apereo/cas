package org.apereo.cas.configuration;

import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

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
import java.util.Arrays;
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
        LOGGER.info("Validating CAS property sources and configuration for active profiles [{}]. Please wait...",
            Arrays.toString(applicationContext.getEnvironment().getActiveProfiles()));
        val validationResults = validateCasConfiguration();
        if (validationResults.isEmpty()) {
            LOGGER.info("Validated CAS property sources and configuration successfully.");
        } else {
            var message = String.join("\n", validationResults);
            message += "\n\nListed settings above are no longer recognized by CAS "
                       + CasVersion.getVersion() + ". They may have been renamed, removed, or relocated "
                       + "to a new namespace in the CAS configuration schema. CAS will ignore such settings to proceed with its normal initialization sequence. "
                       + "Please consult the CAS documentation to review and adjust each setting to find an alternative or remove the "
                       + "definition from the property source. Failure to do so puts the server stability in danger and complicates future upgrades.\n";
            LOGGER.error(message);
        }
        return validationResults;
    }

    private List<String> validateCasConfiguration() {
        return FunctionUtils.doAndHandle(() -> validateConfiguration(CasConfigurationProperties.class));
    }

    private List<String> validateConfiguration(final Class clazz) {
        val beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext.getBeanFactory(), clazz);
        val propertySources = applicationContext.getEnvironment().getPropertySources();
        val conversionService = applicationContext.getEnvironment().getConversionService();
        val handler = new NoUnboundElementsBindHandler(
            new IgnoreTopLevelConverterNotFoundBindHandler(),
            new UnboundElementsSourceFilter());

        val configBinder = new Binder(ConfigurationPropertySources.from(propertySources),
            new PropertySourcesPlaceholdersResolver(propertySources),
            conversionService, null, null, null);

        val validationResults = new ArrayList<String>();
        beans.values().forEach(bean -> {
            try {
                val configBean = ConfigurationPropertiesBean.get(applicationContext, bean, UUID.randomUUID().toString());
                val target = configBean.asBindTarget();
                val annotation = configBean.getAnnotation();
                configBinder.bind(annotation.prefix(), target, handler);
            } catch (final BindException e) {
                var message = "\n".concat(e.getMessage()).concat("\n");
                if (e.getCause() instanceof final UnboundConfigurationPropertiesException ucpe) {
                    message += ucpe.getUnboundProperties()
                        .stream()
                        .map(property -> String.format("%n\t%s = %s (Origin: %s)", property.getName(), property.getValue(), property.getOrigin()))
                        .collect(Collectors.joining("\n"));
                } else {
                    LoggingUtils.error(LOGGER, e);
                }
                validationResults.add(message);
            }
        });
        return validationResults;
    }
}
