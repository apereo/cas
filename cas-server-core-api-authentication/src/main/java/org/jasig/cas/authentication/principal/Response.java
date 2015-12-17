package org.jasig.cas.authentication.principal;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link Response} that is outputted by each service principal.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface Response extends Serializable {
    /** An enumeration of different response types. */
    enum ResponseType {

        /** The post. */
        POST,

        /** The redirect. */
        REDIRECT
    }

    /**
     * Gets attributes.
     *
     * @return the attributes
     */
    Map<String, String> getAttributes();

    /**
     * Gets response type.
     *
     * @return the response type
     */
    ResponseType getResponseType();

    /**
     * Gets url.
     *
     * @return the url
     */
    String getUrl();
}
