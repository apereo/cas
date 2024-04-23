package org.apereo.cas.authentication.principal;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link Response} that is outputted by each service principal.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public interface Response extends Serializable {
    /**
     * Gets attributes.
     *
     * @return the attributes
     */
    Map<String, String> attributes();

    /**
     * Gets response type.
     *
     * @return the response type
     */
    ResponseType responseType();

    /**
     * Gets url.
     *
     * @return the url
     */
    String url();

    /**
     * An enumeration of different response types.
     */
    enum ResponseType {

        /**
         * The post.
         */
        POST,

        /**
         * The redirect.
         */
        REDIRECT,

        /**
         * Response is provided in form of headers.
         */
        HEADER
    }
}
