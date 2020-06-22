package org.apereo.cas.couchdb.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link CouchDbGoogleAuthenticatorAccount}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
@SuperBuilder
public class CouchDbGoogleAuthenticatorAccount extends GoogleAuthenticatorAccount {

    private static final long serialVersionUID = -4286976777933886751L;

    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    /**
     * Update account info from account object.
     *
     * @param acct to be updated
     * @return this
     */
    public static CouchDbGoogleAuthenticatorAccount from(final OneTimeTokenAccount acct) {
        return CouchDbGoogleAuthenticatorAccount.builder()
            .id(acct.getId())
            .name(acct.getName())
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .registrationDate(acct.getRegistrationDate())
            .build();
    }
}
