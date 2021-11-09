package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GroovyAuthenticationExceptionsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@JsonFilter("GroovyAuthenticationExceptionsProperties")
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class GroovyAuthenticationExceptionsProperties extends SpringResourceProperties {
    private static final long serialVersionUID = -1385347572099983874L;
}
