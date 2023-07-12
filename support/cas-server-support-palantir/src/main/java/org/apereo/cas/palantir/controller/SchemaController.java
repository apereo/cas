package org.apereo.cas.palantir.controller;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.util.ReflectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.jakarta.customProperties.HyperSchemaFactoryWrapper;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.lang.reflect.Modifier;

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
        .defaultTypingEnabled(false).build().toObjectMapper();

    @GetMapping(path = "/schema/services", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity schema() {
        val jsonSchemaTypes = ReflectionUtils.findSubclassesInPackage(BaseRegisteredService.class, CentralAuthenticationService.NAMESPACE);
        val results = jsonSchemaTypes
            .stream()
            .filter(type -> !type.isInterface()
                && !type.isAnonymousClass()
                && !Modifier.isAbstract(type.getModifiers())
                && !type.equals(RegexRegisteredService.class))
            .map(type -> FunctionUtils.doUnchecked(() -> {
                val hyperSchemaVisitor = new HyperSchemaFactoryWrapper();
                MAPPER.acceptJsonFormatVisitor(type, hyperSchemaVisitor);
                val schemaGenerator = new JsonSchemaGenerator(MAPPER, hyperSchemaVisitor);
                val jsonSchema = schemaGenerator.generateSchema(type);
                return MAPPER.writeValueAsString(jsonSchema);
            }))
            .toList();
        return ResponseEntity.ok(results);
    }
}
