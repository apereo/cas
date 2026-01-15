package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.authentication.attribute.AttributeDefinition;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * This is {@link AttributeDefinitionsEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Endpoint(id = "attributeDefinitions", defaultAccess = Access.NONE)
public class AttributeDefinitionsEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<@NonNull AttributeDefinitionStore> attributeDefinitionStore;

    public AttributeDefinitionsEndpoint(final CasConfigurationProperties casProperties,
                                        final ObjectProvider<@NonNull AttributeDefinitionStore> attributeDefinitionStore) {
        super(casProperties);
        this.attributeDefinitionStore = attributeDefinitionStore;
    }

    /**
     * Produce all attribute definitions.
     *
     * @return the collection
     */
    @ReadOperation
    @Operation(summary = "Get all attribute definitions registered with CAS")
    public Collection<AttributeDefinition> attributeDefinitions() {
        return attributeDefinitionStore.getObject().getAttributeDefinitions();
    }
}
