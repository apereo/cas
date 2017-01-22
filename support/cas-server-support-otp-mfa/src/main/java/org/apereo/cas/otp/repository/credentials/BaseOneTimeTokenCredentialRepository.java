package org.apereo.cas.otp.repository.credentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link BaseOneTimeTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class BaseOneTimeTokenCredentialRepository implements OneTimeTokenCredentialRepository {
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());
}
