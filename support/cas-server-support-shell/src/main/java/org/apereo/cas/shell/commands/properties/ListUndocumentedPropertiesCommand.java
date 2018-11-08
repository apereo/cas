package org.apereo.cas.shell.commands.properties;

import org.apereo.cas.metadata.CasConfigurationMetadataRepository;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Comparator;
import java.util.Map;

/**
 * This is {@link ListUndocumentedPropertiesCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ShellCommandGroup("CAS Properties")
@ShellComponent
@Slf4j
public class ListUndocumentedPropertiesCommand {
    /**
     * Error message prefix.
     */
    public static final String ERROR_MSG_PREFIX = "Undocumented Property:";

    /**
     * List undocumented settings.
     */
    @ShellMethod(key = "list-undocumented", value = "List all CAS undocumented properties.")
    public void listUndocumented() {
        val repository = new CasConfigurationMetadataRepository();
        repository.getRepository().getAllProperties()
            .entrySet()
            .stream()
            .filter(p -> p.getKey().startsWith("cas.")
                && (StringUtils.isBlank(p.getValue().getShortDescription()) || StringUtils.isBlank(p.getValue().getDescription())))
            .map(Map.Entry::getValue)
            .sorted(Comparator.comparing(ConfigurationMetadataProperty::getId))
            .forEach(p -> LOGGER.error("{} {} @ {}", ERROR_MSG_PREFIX, p.getId(), p.getType()));
    }
}
