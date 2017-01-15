package org.apereo.cas.otp.repository.credentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link BaseOneTimeCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class BaseOneTimeCredentialRepository implements OneTimeCredentialRepository {
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());
}
