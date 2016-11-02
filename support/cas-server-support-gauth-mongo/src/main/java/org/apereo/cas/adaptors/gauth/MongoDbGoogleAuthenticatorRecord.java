package org.apereo.cas.adaptors.gauth;

import java.util.List;

/**
 * This is {@link MongoDbGoogleAuthenticatorRecord}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MongoDbGoogleAuthenticatorRecord {
    private String userName;
    private String secretKey;
    private int validationCode;
    private List<Integer> scratchCodes;

    public MongoDbGoogleAuthenticatorRecord() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public int getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(final int validationCode) {
        this.validationCode = validationCode;
    }

    public List<Integer> getScratchCodes() {
        return scratchCodes;
    }

    public void setScratchCodes(final List<Integer> scratchCodes) {
        this.scratchCodes = scratchCodes;
    }
}
