package org.apereo.cas.configuration.model.support.surrogate;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SurrogateGroovyAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-surrogate-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class SurrogateGroovyAuthenticationProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = 1588367681439517829L;
}
