package org.apereo.cas.overlay.contrib.util;

import org.apereo.cas.configuration.support.PropertyOwner;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jooq.lambda.Unchecked;
import org.jsoup.Jsoup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.util.ReflectionUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@SuperBuilder
public class CasOverlayPropertiesCatalog {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Builder.Default
    private final List<String> modules = new ArrayList<>();

    private final boolean casExclusive;

    @SneakyThrows
    private static Map reasonJsonValueAsMap(final String value) {
        return MAPPER.readValue(value, Map.class);
    }

    private boolean doesPropertyBelongToModule(final ConfigurationMetadataProperty property) {
        val valueHints = property.getHints().getValueHints();
        return valueHints
            .stream()
            .filter(hint -> hint.getDescription().equals(RequiresModule.class.getName()))
            .anyMatch(hint -> {
                val valueHint = ValueHint.class.cast(hint);
                val results = reasonJsonValueAsMap(valueHint.getValue().toString());
                val module = results.get("module");
                return modules.contains(module);
            });
    }

    private static CasReferenceProperty collectReferenceProperty(final ConfigurationMetadataProperty property) {
        val builder = CasReferenceProperty.builder();

        builder.owner(determinePropertySourceType(property));

        property.getHints().getValueHints().forEach(Unchecked.consumer(hint -> {
            val description = hint.getDescription();
            if (StringUtils.isNotBlank(description)) {
                if (description.equals(RequiredProperty.class.getName())) {
                    builder.required(true);
                }
                if (description.equals(RequiresModule.class.getName())) {
                    val results = MAPPER.readValue(hint.getValue().toString(), Map.class);
                    builder.module(results.get("module").toString());
                }
                if (description.equals(PropertyOwner.class.getName())) {
                    val results = MAPPER.readValue(hint.getValue().toString(), Map.class);
                    builder.owner(results.get("owner").toString());
                }
            }
        }));
        builder.type(property.getType());
        var description = StringUtils.defaultIfBlank(property.getDescription(), property.getShortDescription());
        description = StringUtils.isBlank(description) ? StringUtils.EMPTY : Jsoup.parse(description).text();
        description = WordUtils.wrap(description, 100, "\n# ", false);
        
        builder.description(description);
        builder.name(property.getId());
        builder.defaultValue(ObjectUtils.defaultIfNull(property.getDefaultValue(), ""));

        if (property.isDeprecated()) {
            val deprecation = property.getDeprecation();
            builder.deprecationLevel(deprecation.getLevel().toString());
            if (deprecation.getShortReason() != null) {
                builder.deprecationReason(deprecation.getShortReason());
            }
            if (deprecation.getReplacement() != null) {
                builder.deprecationReplacement(deprecation.getReplacement());
            }
        }
        return builder.build();
    }

    @SneakyThrows
    private static String determinePropertySourceType(final ConfigurationMetadataProperty property) {
        val method = ReflectionUtils.findMethod(property.getClass(), "getSourceType");
        if (method == null) {
            return null;
        }
        method.setAccessible(true);
        return (String) method.invoke(property);
    }

    /**
     * Catalog cas properties container.
     *
     * @return the cas properties container
     */
    public CasPropertiesContainer catalog() {
        val repo = new CasConfigurationMetadataRepository();
        val allProperties = repo.getRepository()
            .getAllProperties()
            .entrySet()
            .stream()
            .filter(entry -> casExclusive == CasConfigurationMetadataRepository.isCasProperty(entry.getValue()))
            .collect(Collectors.toList());

        val properties = allProperties
            .stream()
            .filter(entry -> modules.isEmpty() || doesPropertyBelongToModule(entry.getValue()))
            .map(Map.Entry::getValue)
            .map(CasOverlayPropertiesCatalog::collectReferenceProperty)
            .sorted(Comparator.comparing(CasReferenceProperty::getName))
            .collect(Collectors.toCollection(TreeSet::new));
        return new CasPropertiesContainer(properties);
    }

    /**
     * The type Cas properties container.
     */
    @RequiredArgsConstructor
    public static class CasPropertiesContainer {
        private final TreeSet<CasReferenceProperty> properties;

        /**
         * Properties list.
         *
         * @return the list
         */
        public TreeSet<CasReferenceProperty> properties() {
            return this.properties;
        }
    }

    /**
     * The type Cas reference property.
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    @SuperBuilder
    @Getter
    public static class CasReferenceProperty implements Comparable<CasReferenceProperty> {
        private final boolean required;

        private final String module;

        private final String owner;

        private final String type;

        private final String description;

        private final String name;

        private final Object defaultValue;

        private final String deprecationLevel;

        private final String deprecationReason;

        private final String deprecationReplacement;

        private final String sourceType;

        @Override
        public int compareTo(final CasReferenceProperty o) {
            return this.name.compareTo(o.getName());
        }
    }
}
