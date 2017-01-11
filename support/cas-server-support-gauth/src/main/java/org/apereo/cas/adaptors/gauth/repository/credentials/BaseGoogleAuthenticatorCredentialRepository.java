package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.warrenstrange.googleauth.ICredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link BaseGoogleAuthenticatorCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class BaseGoogleAuthenticatorCredentialRepository implements ICredentialRepository {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
}
