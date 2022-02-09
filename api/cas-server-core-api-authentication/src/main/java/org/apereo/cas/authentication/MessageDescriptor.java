package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Simple parameterized message descriptor with a code that refers to a message bundle key and a default
 * message string to use if no message code can be resolved.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface MessageDescriptor extends Serializable {

    /**
     * Gets code.
     *
     * @return the code
     */
    String getCode();

    /**
     * Gets default message.
     *
     * @return the default message
     */
    String getDefaultMessage();

    /**
     * Get params.
     *
     * @return the serializable [ ]
     */
    Serializable[] getParams();
}
