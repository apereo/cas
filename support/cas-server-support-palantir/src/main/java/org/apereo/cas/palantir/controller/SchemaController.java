package org.apereo.cas.palantir.controller;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.generator.SubtypeResolver;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link SchemaController}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RestController
@RequestMapping(PalantirConstants.URL_PATH_PALANTIR)
public class SchemaController {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    /**
     * Generate JSON schema for services.
     *
     * @return the response entity
     */
    @GetMapping(path = "/schema/services",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity schema() {
        val results = generateJsonSchemaFor(BaseRegisteredService.class, List.of(RegexRegisteredService.class.getName()));
        return ResponseEntity.ok(results);
    }

    private static ObjectNode generateJsonSchemaFor(final Class mainTargetType, final List<String> excludeTypes) {
        val configBuilder = new SchemaGeneratorConfigBuilder(MAPPER, SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        configBuilder.with(
                Option.DEFINITIONS_FOR_ALL_OBJECTS,
                Option.DEFINITIONS_FOR_MEMBER_SUPERTYPES,
                Option.SCHEMA_VERSION_INDICATOR)
            .forTypesInGeneral()
            .withSubtypeResolver(new ClassGraphSubtypeResolver(excludeTypes));
        val config = configBuilder.build();
        val generator = new SchemaGenerator(config);
        return generator.generateSchema(mainTargetType);
    }

    private static class ClassGraphSubtypeResolver implements SubtypeResolver {
        private final ClassGraph classGraphConfig;
        private ScanResult scanResult;

        ClassGraphSubtypeResolver(final List<String> excludeTypes) {
            classGraphConfig = new ClassGraph()
                .enableClassInfo()
                .enableInterClassDependencies()
                .rejectClasses(excludeTypes.toArray(ArrayUtils.EMPTY_STRING_ARRAY))
                .acceptPackages(CentralAuthenticationService.NAMESPACE);
        }

        private ScanResult getScanResult() {
            if (scanResult == null) {
                scanResult = classGraphConfig.scan();
            }
            return scanResult;
        }

        @Override
        public void resetAfterSchemaGenerationFinished() {
            if (scanResult != null) {
                scanResult.close();
                scanResult = null;
            }
        }

        @Override
        public List<ResolvedType> findSubtypes(final ResolvedType declaredType, final SchemaGenerationContext context) {
            if (!declaredType.getErasedType().equals(Object.class)) {
                val subtypes = declaredType.isInterface()
                    ? getScanResult().getClassesImplementing(declaredType.getErasedType())
                    : getScanResult().getSubclasses(declaredType.getErasedType());
                if (!subtypes.isEmpty()) {
                    val typeContext = context.getTypeContext();
                    return subtypes.loadClasses(true)
                        .stream()
                        .map(subclass -> typeContext.resolveSubtype(declaredType, subclass))
                        .collect(Collectors.toList());
                }
            }
            return null;
        }
    }
}
