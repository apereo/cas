package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link GoogleAuthenticatorAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class GoogleAuthenticatorAccount implements Serializable, Comparable<GoogleAuthenticatorAccount> {
    private static final long serialVersionUID = -8289105320642735252L;

    private final String secretKey;
    private final int validationCode;
    private final List<Integer> scratchCodes;
    private final String userId;

    /**
     * Instantiates a new Google authenticator account.
     *
     * @param uid            the uid
     * @param secretKey      the secret key
     * @param validationCode the validation code
     * @param scratchCodes   the scratch codes
     */
    public GoogleAuthenticatorAccount(final String uid, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        this.secretKey = secretKey;
        this.validationCode = validationCode;
        this.scratchCodes = scratchCodes;
        this.userId = uid;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public int getValidationCode() {
        return this.validationCode;
    }

    public List<Integer> getScratchCodes() {
        return this.scratchCodes;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public int compareTo(final GoogleAuthenticatorAccount o) {
        return new CompareToBuilder()
                .append(this.scratchCodes, o.getScratchCodes())
                .append(this.validationCode, o.getValidationCode())
                .append(this.secretKey, o.getSecretKey())
                .append(this.userId, o.getUserId())
                .build();
    }
}
