package org.apereo.cas.adaptors.gauth;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link GoogleAuthenticatorAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorAccount implements Serializable {
    private static final long serialVersionUID = -8289105320642735252L;
    
    private String secretKey;
    private int validationCode;
    private List<Integer> scratchCodes;

    /**
     * Instantiates a new Google authenticator account.
     */
    public GoogleAuthenticatorAccount() {
    }

    /**
     * Instantiates a new Google authenticator account.
     *
     * @param secretKey      the secret key
     * @param validationCode the validation code
     * @param scratchCodes   the scratch codes
     */
    public GoogleAuthenticatorAccount(final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        this.secretKey = secretKey;
        this.validationCode = validationCode;
        this.scratchCodes = scratchCodes;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public int getValidationCode() {
        return this.validationCode;
    }

    public void setValidationCode(final int validationCode) {
        this.validationCode = validationCode;
    }

    public List<Integer> getScratchCodes() {
        return this.scratchCodes;
    }

    public void setScratchCodes(final List<Integer> scratchCodes) {
        this.scratchCodes = scratchCodes;
    }
}
