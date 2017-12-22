package org.apereo.cas.support.rest.factory;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link TicketStatusResourcePreprocessor}.
 *
 * @author Francesco Chicchiriccò
 * @since 5.2.2
 */
@FunctionalInterface
public interface TicketStatusResourcePreprocessor {

    /**
     * Preprocess the ticket id before validation.
     *
     * @param id ticket id
     * @param request raw HttpServletRequest used to call this method
     * @return the ticket id after preprocessing
     */
    String preprocess(String id, HttpServletRequest request);
}
