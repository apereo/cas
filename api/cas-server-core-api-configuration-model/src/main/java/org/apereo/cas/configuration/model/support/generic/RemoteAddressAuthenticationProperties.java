package org.apereo.cas.configuration.model.support.generic;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * Configuration properties class for remote.authn.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-generic-remote-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class RemoteAddressAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 573409035023089696L;

    /**
     * The authorized network address to allow for authentication.
     */
    @RequiredProperty
    private String ipAddressRange = StringUtils.EMPTY;

    /**
     * The name of the authentication handler.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private Integer order;
}
