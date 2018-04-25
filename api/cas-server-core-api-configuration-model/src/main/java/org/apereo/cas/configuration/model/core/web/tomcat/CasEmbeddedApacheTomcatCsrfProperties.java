package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link CasEmbeddedApacheTomcatCsrfProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")

@Getter
@Setter
public class CasEmbeddedApacheTomcatCsrfProperties implements Serializable {

    private static final long serialVersionUID = -32143821503580896L;

    /**
     * Enable filter.
     */
    private boolean enabled;
}
