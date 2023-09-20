package org.apereo.cas.palantir.schema;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import lombok.experimental.UtilityClass;
import lombok.val;
import java.util.List;

/**
 * This is {@link SchemaGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@UtilityClass
public class SchemaGenerator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    public static ObjectNode generate(final Class mainTargetType, final List<String> excludeTypes) {
        val jacksonModule = new JacksonModule(
            JacksonOption.ALWAYS_REF_SUBTYPES,
            JacksonOption.INLINE_TRANSFORMED_SUBTYPES);
        val configBuilder = new SchemaGeneratorConfigBuilder(MAPPER, SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON).with(jacksonModule);
        configBuilder.with(
                Option.DEFINITIONS_FOR_ALL_OBJECTS,
                Option.DEFINITIONS_FOR_MEMBER_SUPERTYPES,
                Option.SCHEMA_VERSION_INDICATOR)
            .forTypesInGeneral()
            .withSubtypeResolver(new ClassGraphSubtypeResolver(excludeTypes));
        val config = configBuilder.build();
        val generator = new com.github.victools.jsonschema.generator.SchemaGenerator(config);
        return generator.generateSchema(mainTargetType);
    }
}
