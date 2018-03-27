package org.apereo.cas.configuration.model.support.generic;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties class for remote.authn.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-generic-remote-webflow")
@Slf4j
@Getter
@Setter
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
}
