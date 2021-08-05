package org.apereo.cas.support.oauth.validator;

import org.pac4j.core.context.JEEContext;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20RequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface OAuth20RequestValidator extends Ordered {

    /**
     * Validate request.
     *
     * @param context the context
     * @return true/false
     */
    boolean validate(JEEContext context);

    /**
     * Supports request?
     *
     * @param context the context
     * @return true/false
     */
    boolean supports(JEEContext context);

}
