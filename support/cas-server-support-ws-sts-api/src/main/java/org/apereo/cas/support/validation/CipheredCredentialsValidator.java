package org.apereo.cas.support.validation;

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

    @Override
    public Credential validate(final Credential credential, final RequestData requestData) throws WSSecurityException {
        if (credential != null && credential.getUsernametoken() != null) {
            val usernameToken = credential.getUsernametoken();
            val uid = usernameToken.getName();
            val psw = usernameToken.getPassword();
            if (cipherExecutor.decode(psw).equals(uid)) {
                return credential;
            }
        }
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
    }
}
