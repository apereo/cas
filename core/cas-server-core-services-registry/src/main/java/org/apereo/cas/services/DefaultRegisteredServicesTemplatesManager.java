package org.apereo.cas.services;

import org.apereo.cas.configuration.model.core.services.ServiceRegistryProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.util.Comparator;

/**
 * This is {@link DefaultRegisteredServicesTemplatesManager}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */

@Slf4j
@RequiredArgsConstructor
public class DefaultRegisteredServicesTemplatesManager implements RegisteredServicesTemplatesManager {

    private final ServiceRegistryProperties properties;

    private final StringSerializer<RegisteredService> registeredServiceSerializer;

    @Override
    public RegisteredService apply(final RegisteredService registeredService) {
        val location = properties.getTemplates().getDirectory().getLocation();
        if (!ResourceUtils.doesResourceExist(location)) {
            LOGGER.trace("Registerered service template directory [{}] does not exist", location);
            return registeredService;
        }

        return FunctionUtils.doAndHandle(() -> {
            val resource = ResourceUtils.prepareClasspathResourceIfNeeded(location);
            val files = FileUtils.listFiles(resource.getFile(), new String[]{"json"}, true);
            LOGGER.trace("Found [{}] template registered service definition(s)", files.size());

            val templateDefinition = files.stream()
                .filter(registeredServiceSerializer::supports)
                .map(registeredServiceSerializer::from)
                .sorted(Comparator.comparingInt(RegisteredService::getEvaluationOrder))
                .filter(templateService -> templateService.getClass().equals(registeredService.getClass())
                                           && templateService.getTemplateName().equalsIgnoreCase(registeredService.getTemplateName()))
                .findFirst();
            if (templateDefinition.isEmpty()) {
                LOGGER.trace("Registerered service [{}] is not linked to a registered service template definition in [{}]. "
                             + "Service definition will be returned as is, without any template processing.",
                    registeredService.getName(), location);
                return registeredService;
            }

            val templateService = templateDefinition.get();
            LOGGER.trace("Applying template service definition [{}] to service [{}]", templateService, registeredService.getName());
            val result = registeredServiceSerializer.merge(templateService, registeredService);
            LOGGER.debug("Resulting service definition after merging with template [{}] is:\n[{}]", templateService.getName(), result);
            return result;
        }, e -> registeredService).get();
    }
}
