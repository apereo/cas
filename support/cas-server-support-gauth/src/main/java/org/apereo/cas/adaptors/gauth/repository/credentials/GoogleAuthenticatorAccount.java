package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
     * @param userId         the uid
     * @param secretKey      the secret key
     * @param validationCode the validation code
     * @param scratchCodes   the scratch codes
     */
    @JsonCreator
    public GoogleAuthenticatorAccount(@JsonProperty("userId") final String userId,
                                      @JsonProperty("secretKey") final String secretKey,
                                      @JsonProperty("validationCode") final int validationCode,
                                      @JsonProperty("scratchCodes") final List<Integer> scratchCodes) {
        this.secretKey = secretKey;
        this.validationCode = validationCode;
        this.scratchCodes = scratchCodes;
        this.userId = userId;
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
