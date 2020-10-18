package org.apereo.cas.overlay.contrib.util;

import org.apereo.cas.configuration.support.PropertyOwner;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.jooq.lambda.Unchecked;
import org.jsoup.Jsoup;
import org.springframework.boot.configurationmetadata.ValueHint;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@SuperBuilder
public class CasOverlayPropertiesCatalog {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final String module;

    private final boolean casExclusive;

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

        val properties = new TreeSet<CasReferenceProperty>();
        allProperties.forEach(entry -> {
            try {
                val value = entry.getValue();
                val valueHints = value.getHints().getValueHints();
                val propsByModule = valueHints
                    .stream()
                    .filter(hint -> hint.getDescription().equals(RequiresModule.class.getName()))
                    .filter(Unchecked.predicate(h -> {
                        if (module != null) {
                            var hint = ValueHint.class.cast(h);
                            val results = MAPPER.readValue(hint.getValue().toString(), Map.class);
                            val owner = results.get("module");
                            return owner.equals(module);
                        }
                        return true;
                    }))
                    .map(hint -> value)
                    .collect(Collectors.toList());

                propsByModule.forEach(prop -> {
                    val builder = CasReferenceProperty.builder();
                    prop.getHints().getValueHints().forEach(Unchecked.consumer(hint -> {
                        if (hint.getDescription().equals(RequiredProperty.class.getName())) {
                            builder.required(true);
                        }
                        if (hint.getDescription().equals(RequiresModule.class.getName())) {
                            val results = MAPPER.readValue(hint.getValue().toString(), Map.class);
                            builder.module(results.get("module").toString());
                        }
                        if (hint.getDescription().equals(PropertyOwner.class.getName())) {
                            val results = MAPPER.readValue(hint.getValue().toString(), Map.class);
                            builder.owner(results.get("owner").toString());
                        }
                    }));
                    builder.type(prop.getType());
                    val desc = Jsoup.parse(prop.getDescription()).text();
                    builder.description(desc);
                    builder.name(prop.getName());
                    builder.defaultValue(ObjectUtils.defaultIfNull(prop.getDefaultValue(), ""));

                    if (prop.isDeprecated()) {
                        val deprecation = prop.getDeprecation();
                        builder.deprecationLevel(deprecation.getLevel().toString());
                        if (deprecation.getShortReason() != null) {
                            builder.deprecationReason(deprecation.getShortReason());
                        }
                        if (deprecation.getReplacement() != null) {
                            builder.deprecationReplacement(deprecation.getReplacement());
                        }
                    }
                    properties.add(builder.build());
                });

            } catch (final Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        });
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

        @Override
        public int compareTo(final CasReferenceProperty o) {
            return this.name.compareTo(o.getName());
        }
    }
}
