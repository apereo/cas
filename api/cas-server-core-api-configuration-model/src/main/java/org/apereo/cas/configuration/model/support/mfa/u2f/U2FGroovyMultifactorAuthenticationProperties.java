package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link U2FGroovyMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-u2f")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("U2FGroovyMultifactorAuthenticationProperties")
public class U2FGroovyMultifactorAuthenticationProperties extends SpringResourceProperties {
    private static final long serialVersionUID = -1261683393319585262L;
}
