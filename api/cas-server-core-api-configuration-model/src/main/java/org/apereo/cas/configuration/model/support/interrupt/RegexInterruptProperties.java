package org.apereo.cas.configuration.model.support.interrupt;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link RegexInterruptProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-interrupt-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("RegexInterruptProperties")
public class RegexInterruptProperties implements Serializable {
    private static final long serialVersionUID = 2169027840047126083L;

    /**
     * A regex pattern on the attribute name that if matches will successfully
     * complete the first condition for the interrupt notifications trigger.
     */
    @RequiredProperty
    private String attributeName;

    /**
     * A regex pattern on the attribute value that if matches will successfully
     * complete the first condition for the interrupt notifications trigger.
     */
    @RequiredProperty
    private String attributeValue;
}
