package org.apereo.cas.support.validation;

import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.message.token.UsernameToken;
import org.apache.wss4j.dom.validate.Credential;
import org.apache.wss4j.dom.validate.Validator;
import org.apereo.cas.CipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link CipheredCredentialsValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CipheredCredentialsValidator implements Validator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CipheredCredentialsValidator.class);
    
    private final CipherExecutor cipherExecutor;

    public CipheredCredentialsValidator(final CipherExecutor cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    public Credential validate(final Credential credential, final RequestData requestData) throws WSSecurityException {
        if (credential != null && credential.getUsernametoken() != null) {
            final UsernameToken usernameToken = credential.getUsernametoken();
            final String uid = usernameToken.getName();
            final String psw = usernameToken.getPassword();
            if (cipherExecutor.decode(psw).equals(uid)) {
                return credential;
            }
        }
        throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_AUTHENTICATION);
    }
}
