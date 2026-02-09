package org.apereo.cas.configuration.model.support.wsfed;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GroovyWsFederationDelegationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated Since 8.0.0, WS-Federation delegation support is deprecated and scheduled for removal.
 */
@RequiresModule(name = "cas-server-support-wsfederation-webflow")
@Getter
@Setter
@Accessors(chain = true)
@Deprecated(since = "8.0.0", forRemoval = true)
public class GroovyWsFederationDelegationProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = 8079027843747126083L;
}
