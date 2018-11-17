package org.apereo.cas.configuration.model.support.openid;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OpenIdProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-openid")
@Getter
@Setter
public class OpenIdProperties implements Serializable {

    private static final long serialVersionUID = -2935759289483632610L;

    /**
     * Principal construction settings.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * Whether relying party identifies should be enforced.
     * This is used during the realm verification process.
     */
    private boolean enforceRpId;

    /**
     * Name of the underlying authentication handler.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private Integer order;
}
