package org.apereo.cas.configuration.model.core.rest;

import module java.base;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestRegisteredServicesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-rest")
@Getter
@Setter
@Accessors(chain = true)
public class RestRegisteredServicesProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -1822107478273171342L;

    /**
     * Authorization attribute name required by the REST endpoint in order to allow for the requested operation.
     * Attribute must be resolvable by the authenticated principal, or must have been already.
     */
    @RequiredProperty
    private String attributeName;

    /**
     * Matching authorization attribute value, pulled from the attribute
     * required by the REST endpoint in order to allow for the requested operation.
     * The attribute value may also be constructed as a regex pattern.
     */
    @RequiredProperty
    @RegularExpressionCapable
    private String attributeValue;
}
