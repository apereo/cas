package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * This is {@link AttributeDefinitionsEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Endpoint(id = "attributeDefinitions", defaultAccess = Access.NONE)
public class AttributeDefinitionsEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<@NonNull AttributeDefinitionStore> attributeDefinitionStore;

    public AttributeDefinitionsEndpoint(final CasConfigurationProperties casProperties,
                                        final ConfigurableApplicationContext applicationContext,
                                        final ObjectProvider<@NonNull AttributeDefinitionStore> attributeDefinitionStore) {
        super(casProperties, applicationContext);
        this.attributeDefinitionStore = attributeDefinitionStore;
    }

    /**
     * Produce all attribute definitions.
     *
     * @return the collection
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all attribute definitions registered with CAS")
    public Collection<AttributeDefinition> attributeDefinitions() {
        return attributeDefinitionStore.getObject().getAttributeDefinitions();
    }

    /**
     * Register attribute definition.
     *
     * @param attributeDefinitions the attribute definitions
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register new attribute definitions with CAS",
        parameters = @Parameter(
            description = "List of attribute definitions",
            array = @ArraySchema(
                schema = @Schema(implementation = AttributeDefinition.class)
            )
        ))
    public void registerAttributeDefinition(@RequestBody final List<AttributeDefinition> attributeDefinitions) {
        for (val defn : attributeDefinitions) {
            attributeDefinitionStore.getObject().registerAttributeDefinition(defn);
        }
    }
}
