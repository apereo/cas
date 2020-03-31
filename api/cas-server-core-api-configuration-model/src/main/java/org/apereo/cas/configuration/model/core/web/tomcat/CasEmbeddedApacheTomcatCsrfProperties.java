package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatCsrfProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Accessors(chain = true)
@Setter
public class CasEmbeddedApacheTomcatCsrfProperties implements Serializable {

    private static final long serialVersionUID = -32143821503580896L;

    /**
     * Enable filter.
     */
    private boolean enabled;
}
