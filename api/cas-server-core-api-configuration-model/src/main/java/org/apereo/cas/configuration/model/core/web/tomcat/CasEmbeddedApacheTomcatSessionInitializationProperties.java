package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatSessionInitializationProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Accessors(chain = true)
@Setter

public class CasEmbeddedApacheTomcatSessionInitializationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -42243821503580896L;

    /**
     * Enable filter.
     */
    @RequiredProperty
    private boolean enabled;
}
