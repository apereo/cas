package org.apereo.cas.configuration.model.support.pac4j.saml;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link Pac4jSamlClientMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-pac4j-saml")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jSamlClientMetadataProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -562839796533384951L;

    /**
     * The metadata location of the identity provider that is to handle authentications.
     * The location can be specified as a direct absolute path to the metadata file
     * or it may also be a URL to the identity provider's metadata endpoint.
     */
    @RequiredProperty
    private String identityProviderMetadataPath;
    
    /**
     * SAML2 service provider metadata settings.
     */
    @NestedConfigurationProperty
    private Pac4jSamlServiceProviderMetadataProperties serviceProvider = new Pac4jSamlServiceProviderMetadataProperties();
}
