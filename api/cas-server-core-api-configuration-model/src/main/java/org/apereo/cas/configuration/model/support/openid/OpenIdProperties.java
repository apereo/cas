package org.apereo.cas.configuration.model.support.openid;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OpenIdProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated 6.2
 */
@RequiresModule(name = "cas-server-support-openid-webflow")
@Getter
@Setter
@Deprecated(since = "6.2.0")
@Accessors(chain = true)
public class OpenIdProperties implements Serializable {

    private static final long serialVersionUID = -2935759289483632610L;

    /**
     * Principal construction settings.
     * @deprecated Since 6.2
     */
    @NestedConfigurationProperty
    @Deprecated(since = "6.2.0")
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * Whether relying party identifies should be enforced.
     * This is used during the realm verification process.
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
    private boolean enforceRpId;

    /**
     * Name of the underlying authentication handler.
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
    private String name;

    /**
     * Order of the authentication handler in the chain.
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
    private Integer order;
}
