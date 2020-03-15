package org.apereo.cas.configuration.model.core.web.view;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link Cas10ViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class Cas10ViewProperties implements Serializable {
    private static final long serialVersionUID = -1154879759474698223L;

    /**
     * Indicates how attributes in the final validation response should be formatted.
     * Options available are:
     *
     * <ul>
     * <li>{@code DEFAULT}: The default option implements the rendering strategy specified by the CAS protocol.</li>
     * <li>{@code VALUES_PER_LINE}: Includes the attribute value on each single line. (Values are comma-separated, if multiple).</li>
     * </ul>
     */
    private ValidationAttributesRendererTypes attributeRendererType = ValidationAttributesRendererTypes.DEFAULT;

    public enum ValidationAttributesRendererTypes {
        /**
         * Render attributes using CAS protocol suggestions.
         */
        DEFAULT,
        /**
         * Inline attribute value(s) on each line.
         */
        VALUES_PER_LINE
    }

}
