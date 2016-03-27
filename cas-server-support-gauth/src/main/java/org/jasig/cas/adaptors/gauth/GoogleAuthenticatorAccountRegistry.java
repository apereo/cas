package org.jasig.cas.adaptors.gauth;

import com.warrenstrange.googleauth.ICredentialRepository;

/**
 * General contract that allows one to determine whether
 * a particular google authenticator account
 * is allowed to participate in the authentication.
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface GoogleAuthenticatorAccountRegistry extends ICredentialRepository {
    
}
