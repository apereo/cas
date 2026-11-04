package org.apereo.cas.support.validation;

import module java.base;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.validate.Credential;
import org.apache.wss4j.dom.validate.Validator;

/**
 * This is {@link CipheredCredentialsValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class CipheredCredentialsValidator implements Validator {
    private final CipherExecutor cipherExecutor;

    /**
     * Accept the credential when its password decodes to its user name. A password that the cipher cannot decode
     * (one whose signature cannot be verified decodes to null) is refused like a password that does not match.
     *
     * @param credential  the credential
     * @param requestData the request data
     * @return the credential
     * @throws WSSecurityException when the credential is refused
     */
    @Override
    public Credential validate(final Credential credential, final RequestData requestData) throws WSSecurityException {
        if (credential != null && credential.getUsernametoken() != null) {
            val usernameToken = credential.getUsernametoken();
            val uid = usernameToken.getName();
            val decodedPassword = cipherExecutor.decode(usernameToken.getPassword());
            if (decodedPassword != null && decodedPassword.equals(uid)) {
                return credential;
            }
        }
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
    }
}
