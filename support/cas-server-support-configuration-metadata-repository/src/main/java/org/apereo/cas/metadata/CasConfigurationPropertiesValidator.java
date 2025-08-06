package org.apereo.cas.metadata;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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

    @Setter
    private List<Class> configurationPropertyClasses = CollectionUtils.wrapList(CasConfigurationProperties.class);

    /**
     * Validate the cas configuration properties
     * from property sources found in the application
     * context and report back results..
     *
     * @return the list
     */
    public List<CasConfigurationPropertyBindingResult> validate() {
        LOGGER.info("Validating CAS property sources and configuration for active profiles [{}]. Please wait...",
            Arrays.toString(applicationContext.getEnvironment().getActiveProfiles()));
        val validationResults = validateCasConfiguration();
        if (validationResults.isEmpty()) {
            LOGGER.info("Validated CAS property sources and configuration successfully.");
        } else {
            val unknownProperties = validationResults
                .stream()
                .filter(result -> result.status() == CasConfigurationPropertyBindingResult.BindingStatus.UNKNOWN)
                .map(CasConfigurationPropertyBindingResult::toString)
                .collect(Collectors.joining("\n"));
            if (!unknownProperties.isEmpty()) {
                val message = """
                    The following settings are not recognized by CAS {}. They may have been renamed, removed, or relocated \
                    to a new namespace in the CAS configuration schema. CAS will ignore such settings to proceed with its normal initialization sequence. \
                    Please consult the CAS documentation to review and adjust each setting to find an alternative or remove the \
                    definition from the property source. Failure to do so puts the server stability in danger and complicates future upgrades.
                    {}
                    """.stripIndent().stripLeading();
                LOGGER.error(message, CasVersion.getVersion(), unknownProperties);
            }
        }
        return validationResults;
    }

    private List<CasConfigurationPropertyBindingResult> validateCasConfiguration() {
        return FunctionUtils.doAndHandle(() -> configurationPropertyClasses.stream().map(this::validateConfiguration).flatMap(List::stream).toList());
    }

    private List<CasConfigurationPropertyBindingResult> validateConfiguration(final Class clazz) {
        val beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext.getBeanFactory(), clazz);
        val propertySources = applicationContext.getEnvironment().getPropertySources();
        val conversionService = applicationContext.getEnvironment().getConversionService();
        val deprecatedHandler = new DeprecatedElementsBindHandler();
        val handler = new NoUnboundElementsBindHandler(
            new IgnoreTopLevelConverterNotFoundBindHandler(deprecatedHandler),
            new UnboundElementsSourceFilter());

        val configBinder = new Binder(ConfigurationPropertySources.from(propertySources),
            new PropertySourcesPlaceholdersResolver(propertySources),
            conversionService, null, null, null);

        val validationResults = new ArrayList<CasConfigurationPropertyBindingResult>();
        beans.values().forEach(bean -> {
            try {
                val configBean = ConfigurationPropertiesBean.get(applicationContext, bean, UUID.randomUUID().toString());
                val target = configBean.asBindTarget();
                val annotation = configBean.getAnnotation();
                configBinder.bind(annotation.prefix(), target, handler);
            } catch (final BindException e) {
                if (e.getCause() instanceof final UnboundConfigurationPropertiesException ucpe) {
                    validationResults.addAll(ucpe.getUnboundProperties()
                        .stream()
                        .map(prop -> new CasConfigurationPropertyBindingResult(prop,
                            CasConfigurationPropertyBindingResult.BindingStatus.UNKNOWN))
                        .toList());
                } else {
                    LoggingUtils.error(LOGGER, e);
                }
            }
        });
        deprecatedHandler.getDeprecatedProperties()
            .stream()
            .map(dcpe -> new CasConfigurationPropertyBindingResult(dcpe,
                CasConfigurationPropertyBindingResult.BindingStatus.DEPRECATED))
            .forEach(validationResults::add);
        return validationResults;
    }

    /**
     * Print report for binding results and failures.
     *
     * @param results the results
     */
    public void printReport(final List<CasConfigurationPropertyBindingResult> results) {
        val metadataRepository = applicationContext.getBean(CasConfigurationMetadataRepository.BEAN_NAME, CasConfigurationMetadataRepository.class);
        var messages = results
            .stream()
            .map(entry -> {
                val propertyName = entry.property().getName().toString();
                val query = ConfigurationMetadataCatalogQuery.builder()
                    .queryType(ConfigurationMetadataCatalogQuery.QueryTypes.CAS)
                    .queryFilter(property -> Strings.CI.equals(propertyName, property.getName()) && property.isDeprecated())
                    .build();
                val container = CasConfigurationMetadataCatalog.query(query, metadataRepository);
                val status = entry.status().getLabel();
                if (container.hasProperties()) {
                    return container.properties()
                        .stream()
                        .map(property -> {
                            var propertyLine = "\t- %s Property: %s = %s%n".formatted(status, property.getName(), entry.property().getValue());
                            if (StringUtils.isNotBlank(property.getOwner())) {
                                propertyLine += "\t  Owner: %s%n".formatted(property.getOwner());
                            }
                            if (StringUtils.isNotBlank(property.getShortDescription())) {
                                propertyLine += "\t  Description: %s%n".formatted(property.getShortDescription());
                            }
                            if (StringUtils.isNotBlank(property.getDeprecationReplacement())) {
                                propertyLine += "\t  Replacement: %s%n".formatted(property.getDeprecationReplacement());
                            }
                            if (StringUtils.isNotBlank(property.getDeprecationReason())) {
                                propertyLine += "\t  Reason: %s%n".formatted(property.getDeprecationReason());
                            }
                            return propertyLine;
                        })
                        .toList();
                }
                return List.of("\t- %s Property: %s = %s".formatted(status, propertyName, entry.property().getValue()));
            })
            .flatMap(List::stream)
            .collect(Collectors.joining("\n"));

        if (StringUtils.isNotBlank(messages)) {
            messages = "\nThe following settings are deprecated and scheduled to be removed in future releases of CAS. "
                + "Please review the replacement settings where applicable and update your configuration accordingly.\n\n"
                + messages;
            LOGGER.warn(messages);
        }
    }
}
