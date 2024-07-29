package org.apereo.cas.configuration.model.support.pac4j.saml;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link Pac4jSamlServiceProviderRequestedAttribute}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-saml")
@Getter
@Setter
@Accessors(chain = true)

public class Pac4jSamlServiceProviderRequestedAttribute implements Serializable {
    @Serial
    private static final long serialVersionUID = -862819796533384951L;

    /**
     * Attribute name.
     */
    private String name;

    /**
     * Attribute friendly name.
     */
    private String friendlyName;

    /**
     * Attribute name format.
     */
    private String nameFormat = "urn:oasis:names:tc:SAML:2.0:attrname-format:uri";

    /**
     * Whether this attribute is required and should
     * be marked so in the metadata.
     */
    private boolean required;
}
