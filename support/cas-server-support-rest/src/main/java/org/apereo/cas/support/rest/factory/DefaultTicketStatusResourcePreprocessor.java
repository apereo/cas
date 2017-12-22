package org.apereo.cas.support.rest.factory;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultTicketStatusResourcePreprocessor}.
 *
 * @author Francesco Chicchiriccò
 * @since 5.2.2
 */
public class DefaultTicketStatusResourcePreprocessor implements TicketStatusResourcePreprocessor {

    @Override
    public String preprocess(final String id, final HttpServletRequest request) {
        return id;
    }

}
