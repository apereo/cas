package org.apereo.cas.support.oauth.validator;

import org.pac4j.core.context.J2EContext;

/**
 * This is {@link OAuth20RequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface OAuth20RequestValidator {

    /**
     * Validate request.
     *
     * @param context the context
     * @return the boolean
     */
    boolean validate(J2EContext context);

    /**
     * Supports request?
     *
     * @param context the context
     * @return the boolean
     */
    boolean supports(J2EContext context);

}
