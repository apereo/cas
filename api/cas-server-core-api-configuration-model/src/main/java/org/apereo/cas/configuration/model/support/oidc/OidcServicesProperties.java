package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcServicesProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcServicesProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 1233477683583467669L;

    /**
     * Control the default, initial values for fields that are part of a OIDC service definition.
     * This is defined as a map where the key is the field name (i.e. {@code signIdToken})
     * and the value should be the default value.
     * If a service definition explicitly defines a value for a field, that value
     * will take over and the default defined here will be ignored.
     * If a service definition does not define a value for a field and no defaults are specified
     * for that field, then the default value that is directly assigned to the field in the body
     * of the service definition will take over.
     */
    private final Map<String, String> defaults = new LinkedHashMap<>();
}
