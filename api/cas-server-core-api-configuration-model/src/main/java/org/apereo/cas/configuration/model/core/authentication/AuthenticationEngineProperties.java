package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
@JsonFilter("AuthenticationEngineProperties")
public class AuthenticationEngineProperties implements Serializable {
    private static final long serialVersionUID = -2475347572099983874L;

    /**
     * Groovy script to handle the authentication pre-processor.
     */
    @NestedConfigurationProperty
    private GroovyAuthenticationEngineProcessorProperties groovyPreProcessor = new GroovyAuthenticationEngineProcessorProperties();

    /**
     * Groovy script to handle the authentication post-processor.
     */
    @NestedConfigurationProperty
    private GroovyAuthenticationEngineProcessorProperties groovyPostProcessor = new GroovyAuthenticationEngineProcessorProperties();

}
