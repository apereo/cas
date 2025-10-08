package org.apereo.cas.services;

import org.apereo.cas.configuration.model.core.services.ServiceRegistryProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultRegisteredServicesTemplatesManager}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultRegisteredServicesTemplatesManager implements RegisteredServicesTemplatesManager {

    private final Collection<File> templateDefinitionFiles;

    private final StringSerializer<RegisteredService> registeredServiceSerializer;

    public DefaultRegisteredServicesTemplatesManager(final ServiceRegistryProperties properties,
                                                     final StringSerializer<RegisteredService> registeredServiceSerializer) {
        this.registeredServiceSerializer = registeredServiceSerializer;

        val location = properties.getTemplates().getDirectory().getLocation();
        LOGGER.debug("Attempting to locate service template definitions from [{}]", location);
        templateDefinitionFiles = FunctionUtils.doUnchecked(() -> ResourceUtils.doesResourceExist(location)
            ? FileUtils.listFiles(location.getFile(), new String[]{"json"}, true)
            : new ArrayList<>());
        LOGGER.trace("Found [{}] template registered service definition(s)", templateDefinitionFiles.size());
    }

    @Override
    public RegisteredService apply(final RegisteredService registeredService) {
        if (templateDefinitionFiles.isEmpty() || StringUtils.isBlank(registeredService.getTemplateName())) {
            LOGGER.trace("Registered service template directory contains no template definitions, "
                         + "or registered service [{}] does specify template name(s)", registeredService.getName());
            return registeredService;
        }

        RegisteredService mergeResult = null;
        val templateNames = org.springframework.util.StringUtils.commaDelimitedListToStringArray(registeredService.getTemplateName().trim());
        for (val templateName : templateNames) {
            val templateDefinition = locateTemplateServiceDefinition(registeredService, templateName);
            if (templateDefinition.isEmpty()) {
                LOGGER.warn("Registered service template definition [{}] cannot be found and is not applicable to [{}]",
                    templateName, registeredService.getName());
            } else {
                val templateService = templateDefinition.get();
                LOGGER.trace("Applying template service definition [{}] to service [{}]", templateService, registeredService.getName());
                mergeResult = registeredServiceSerializer.merge(templateService, Objects.requireNonNullElse(mergeResult, registeredService));
                LOGGER.debug("Resulting service definition after merging with template [{}] is:\n[{}]",
                    templateService.getTemplateName(), mergeResult);
            }
        }
        return Objects.requireNonNullElse(mergeResult, registeredService);
    }

    private Optional<RegisteredService> locateTemplateServiceDefinition(final RegisteredService registeredService,
                                                                        final String templateName) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        return templateDefinitionFiles
            .stream()
            .filter(_ -> CasRuntimeHintsRegistrar.notInNativeImage())
            .filter(registeredServiceSerializer::supports)
            .filter(file -> FilenameUtils.getBaseName(file.getAbsolutePath()).equalsIgnoreCase(templateName))
            .map(file -> FunctionUtils.doUnchecked(() -> {
                val templateParams = registeredService.getProperties()
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        val listOfValues = new ArrayList<>(entry.getValue().getValues());
                        val values = listOfValues.size() == 1 ? listOfValues.getFirst() : listOfValues;
                        return Pair.of(entry.getKey(), values);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                val template = scriptFactory.createTemplate(file, templateParams);
                return registeredServiceSerializer.from(template);
            }))
            .sorted(Comparator.comparingInt(RegisteredService::getEvaluationOrder))
            .filter(templateService -> templateService.getClass().equals(registeredService.getClass())
                                       && templateService.getTemplateName().equalsIgnoreCase(templateName))
            .findFirst();
    }
}
