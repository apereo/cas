package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link AuthenticationEngineProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class AuthenticationEngineProperties implements Serializable {
    private static final long serialVersionUID = -2475347572099983874L;

    /**
     * Groovy script to handle the authentication pre-processor.
     */
    private Groovy groovyPreProcessor = new Groovy();

    /**
     * Groovy script to handle the authentication post-processor.
     */
    private Groovy groovyPostProcessor = new Groovy();

    @RequiresModule(name = "cas-server-core-authentication", automated = true)
    @Getter
    @Setter
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 8079027843747126083L;
    }
}
