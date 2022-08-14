package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link Pac4jDelegatedAuthenticationProfileSelectionProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-pac4j")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Pac4jDelegatedAuthenticationProfileSelectionProperties")
public class Pac4jDelegatedAuthenticationProfileSelectionProperties implements Serializable {
    private static final long serialVersionUID = 1478567744591488495L;

    /**
     * Groovy script to execute operations on profile selection.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties groovy = new SpringResourceProperties();
}
