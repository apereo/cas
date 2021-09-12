package org.apereo.cas.configuration.model.support.interrupt;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link InterruptProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-interrupt-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("InterruptProperties")
public class InterruptProperties implements Serializable {
    private static final long serialVersionUID = -4945287309473842615L;

    /**
     * Inquire for interrupt using a JSON resource.
     */
    @NestedConfigurationProperty
    private JsonInterruptProperties json = new JsonInterruptProperties();

    /**
     * Inquire for interrupt using a Groovy resource.
     */
    @NestedConfigurationProperty
    private GroovyInterruptProperties groovy = new GroovyInterruptProperties();

    /**
     * Inquire for interrupt using a REST resource.
     */
    @NestedConfigurationProperty
    private RestfulInterruptProperties rest = new RestfulInterruptProperties();

    /**
     * Inquire for interrupt using a regex pattern operating on attributes.
     */
    @NestedConfigurationProperty
    private RegexInterruptProperties regex = new RegexInterruptProperties();

    /**
     * Core settings for interrupt notifications.
     */
    @NestedConfigurationProperty
    private InterruptCoreProperties core = new InterruptCoreProperties();

    /**
     * Cookie settings.
     */
    @NestedConfigurationProperty
    private InterruptCookieProperties cookie = new InterruptCookieProperties();
}
