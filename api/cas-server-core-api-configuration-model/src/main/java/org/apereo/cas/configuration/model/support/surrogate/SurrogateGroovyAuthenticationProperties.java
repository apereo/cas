package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("SurrogateGroovyAuthenticationProperties")
public class SurrogateGroovyAuthenticationProperties extends SpringResourceProperties {
    private static final long serialVersionUID = 1588367681439517829L;
}
