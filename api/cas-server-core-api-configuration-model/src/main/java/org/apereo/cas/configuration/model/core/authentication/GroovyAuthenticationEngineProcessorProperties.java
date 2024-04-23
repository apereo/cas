package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link GroovyAuthenticationEngineProcessorProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */

@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("GroovyAuthenticationEngineProcessorProperties")
public class GroovyAuthenticationEngineProcessorProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = 8079027843747126083L;
}
