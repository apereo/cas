package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link Pac4jDelegatedAuthenticationJdbcProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationJdbcProperties extends AbstractJpaProperties {
    @Serial
    private static final long serialVersionUID = 3651984897056632608L;
}
