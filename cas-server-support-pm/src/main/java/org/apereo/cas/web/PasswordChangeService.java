package org.apereo.cas.web;

import org.apereo.cas.authentication.Credential;

/**
 * This is {@link PasswordChangeService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface PasswordChangeService {

    /**
     * Execute op.
     *
     * @param c    the credentials
     * @param bean the bean
     * @return true/false
     */
    boolean execute(final Credential c, PasswordChangeBean bean);
}
