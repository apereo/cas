package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * This is {@link GoogleAuthenticatorAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@Table(name = "GoogleAuthenticatorRegistrationRecord")
public class GoogleAuthenticatorAccount extends OneTimeTokenAccount {

    private static final long serialVersionUID = 2441775052626253711L;

    public GoogleAuthenticatorAccount() {
        super();
    }

    /**
     * Instantiates a new Google authenticator account.
     *
     * @param username       the user id
     * @param secretKey      the secret key
     * @param validationCode the validation code
     * @param scratchCodes   the scratch codes
     */
    @JsonCreator
    public GoogleAuthenticatorAccount(@JsonProperty("username") final String username,
                                      @JsonProperty("secretKey") final String secretKey,
                                      @JsonProperty("validationCode") final int validationCode,
                                      @JsonProperty("scratchCodes") final List<Integer> scratchCodes) {
        super(username, secretKey, validationCode, scratchCodes);
    }
}
