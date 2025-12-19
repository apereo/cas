package org.apereo.cas.configuration.model.support.saml.idp;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SamlIdPServicesProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
public class SamlIdPServicesProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 7211477683583467619L;

    /**
     * Control the default, initial values for fields that are part of a SAML service definition.
     * This is defined as a map where the key is the field name (i.e. {@code signAssertions})
     * and the value should be the default value.
     * If a service definition explicitly defines a value for a field, that value
     * will take over and the default defined here will be ignored.
     * If a service definition does not define a value for a field and no defaults are specified
     * for that field, then the default value that is directly assigned to the field in the body
     * of the service definition will take over.
     */
    private final Map<String, String> defaults = new LinkedHashMap<>();
}
