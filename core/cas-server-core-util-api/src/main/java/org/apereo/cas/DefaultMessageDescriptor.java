package org.apereo.cas;

import org.apereo.cas.authentication.MessageDescriptor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * Simple parameterized message descriptor with a code that refers to a message bundle key and a default
 * message string to use if no message code can be resolved.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class DefaultMessageDescriptor implements MessageDescriptor {

    /**
     * Serialization support.
     */
    private static final long serialVersionUID = 1227390629186486032L;

    private final String code;

    private final String defaultMessage;

    private final Serializable[] params;

    /**
     * Instantiates a new message.
     *
     * @param code the code
     */
    public DefaultMessageDescriptor(final String code) {
        this(code, code, null);
    }

}
