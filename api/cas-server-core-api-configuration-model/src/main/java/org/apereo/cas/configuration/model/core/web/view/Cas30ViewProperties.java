package org.apereo.cas.configuration.model.core.web.view;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link Cas30ViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Cas30ViewProperties")
public class Cas30ViewProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 2345062034300650858L;

    /**
     * The relative location of the CAS3 success validation bean.
     */
    private String success = "protocol/3.0/casServiceValidationSuccess";

    /**
     * The relative location of the CAS3 success validation bean.
     */
    private String failure = "protocol/3.0/casServiceValidationFailure";

    /**
     * Indicates how attributes in the final validation response should be formatted.
     */
    private ValidationAttributesRendererTypes attributeRendererType = ValidationAttributesRendererTypes.DEFAULT;

    public enum ValidationAttributesRendererTypes {
        /**
         * Render attributes using CAS protocol suggestions.
         */
        DEFAULT,
        /**
         * Inline attribute name/value as XML attributes.
         */
        INLINE
    }

}
