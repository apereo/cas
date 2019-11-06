package org.apereo.cas.support.saml.services;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.jooq.lambda.Unchecked;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.*;

/**
 * Class that reads in attribute definitions to be used in SAML responses from a JSON file.
 *
 * @author Travis Schmidt
 * @since 6.2.0
 */
@Slf4j
@Getter
public class SamlIdpAttributeResolver {

    private final CasConfigurationProperties casProperties;
    private final ObjectMapper objectMapper;
    private final FileWatcherService watcher;

    private Map<String, SamlIdpAttributeDefinition> attributeDefinitions;

    @SneakyThrows
    public SamlIdpAttributeResolver(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
        this.objectMapper = new ObjectMapper(getJsonFactory()).findAndRegisterModules();
        val resource = casProperties.getAuthn().getSamlIdp().getAttributesDefinition();
        loadResource(resource);
        this.watcher = new FileWatcherService(resource.getFile(),
                                         Unchecked.consumer(file -> loadResource(new FileSystemResource(file))));
        this.watcher.start(getClass().getSimpleName());
    }

    @SneakyThrows
    private void loadResource(final Resource res) {
        LOGGER.debug("Reloading Attribute Definitions");
        try (Reader reader = new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8)) {
            val personList = new TypeReference<Map<String, SamlIdpAttributeDefinition>>() {
            };
            this.attributeDefinitions = this.objectMapper.readValue(JsonValue.readHjson(reader).toString(), personList);
        }
    }

    protected JsonFactory getJsonFactory() {
        return null;
    }

    public Map<String, List<Object>> getAttributes(final Map<String, List<Object>> values,
                                                   final List<String> allowed) {
        try {
            val defs = attributeDefinitions.entrySet().stream()
                    .filter(e -> allowed.contains(e.getKey()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            return defs.entrySet().stream()
                    .map(at -> getAttribute(at.getKey(), at.getValue(), values))
                    .filter(Objects::nonNull)
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Saml Attributes cannot be loaded");
        return new HashMap<>(0);
    }

    private Map.Entry<String, List<Object>> getAttribute(final String id,
                                                         final SamlIdpAttributeDefinition def,
                                                         final Map<String, List<Object>> values) {

        LOGGER.debug("Def id = [{}], attribute = [{}], scoped = [{}]", id, def.getAttribute(), def.isScoped());
        val key = StringUtils.isNotBlank(def.getAttribute()) ? def.getAttribute() : id;
        if (!values.containsKey(key) || values.get(key) == null) {
            return null;
        }
        var value = values.get(key);
        if (StringUtils.isNotBlank(def.getScript())) {
            value = runScript(def.getScript(), values, value);
        }
        if (def.isScoped()) {
            value = value.stream().map(v -> (Object) (v + "@" + casProperties.getAuthn().getSamlIdp().getScope())).collect(toList());
        }
        if (StringUtils.isNotBlank(def.getTemplate())) {
            value = value.stream().map(v -> (Object) (MessageFormat.format(def.getTemplate(), v))).collect(toList());
        }
        return Map.entry(id, value);
    }

    private List<Object> runScript(final String script, final Map<String, List<Object>> attributes, final List<Object> value) {
        try {
            val res = ResourceUtils.getResourceFrom(script);
            val groovy = new WatchableGroovyScriptResource(res);
            val args = new Object[]{attributes, value, LOGGER};
            return groovy.execute(args, List.class, true);
        } catch(final Exception exc) {
            LOGGER.error(exc.getMessage(), exc);
        }
        return value;
    }

}
