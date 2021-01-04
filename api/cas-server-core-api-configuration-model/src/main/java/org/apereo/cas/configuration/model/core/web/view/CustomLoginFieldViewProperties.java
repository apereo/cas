package org.apereo.cas.configuration.model.core.web.view;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link CustomLoginFieldViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@JsonFilter("CustomLoginFieldViewProperties")
public class CustomLoginFieldViewProperties implements Serializable {
    private static final long serialVersionUID = -7122345678378395582L;

    /**
     * The key for this field found in the message bundle
     * used to present a label/text in CAS views.
     */
    private String messageBundleKey;

    /**
     * Whether this field is required to have a value.
     */
    private boolean required;

    /**
     * The id of the custom converter to use to convert bound property values.
     */
    private String converter;
}
