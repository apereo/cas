package org.apereo.cas.configuration.model.support.pac4j.saml;

import module java.base;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jSamlServiceProviderMetadataFileSystemProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-saml")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jSamlServiceProviderMetadataFileSystemProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -992809796533384951L;

    /**
     * Location of the SP metadata to use and generate
     * on the file system. If the metadata file already exists,
     * it will be ignored and reused.
     */
    @RequiredProperty
    private String location;
}
