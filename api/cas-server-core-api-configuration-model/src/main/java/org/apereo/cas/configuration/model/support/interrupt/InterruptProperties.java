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
     * A regex pattern on the attribute name that if matches will successfully
     * complete the first condition for the interrupt notifications trigger.
     */
    private String attributeName;

    /**
     * A regex pattern on the attribute value that if matches will successfully
     * complete the first condition for the interrupt notifications trigger.
     */
    private String attributeValue;

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
}
