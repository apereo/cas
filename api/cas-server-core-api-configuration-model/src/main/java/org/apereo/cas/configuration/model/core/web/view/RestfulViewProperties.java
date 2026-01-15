package org.apereo.cas.configuration.model.core.web.view;

import module java.base;
import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link RestfulViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter

public class RestfulViewProperties extends RestEndpointProperties {
    @Serial
    private static final long serialVersionUID = -8102345678378393382L;
}
