package org.apereo.cas.couchdb.gauth;

import org.apereo.cas.authentication.OneTimeTokenAccount;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * This is {@link CouchDbOneTimeTokenAccount}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public class CouchDbOneTimeTokenAccount extends OneTimeTokenAccount {

    private static final long serialVersionUID = -4286976777933886751L;

    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbOneTimeTokenAccount(@JsonProperty("_id") final String cid, //NOPMD
                                      @JsonProperty("_rev") final String rev,
                                      @JsonProperty("id") final long id,
                                      @JsonProperty("username") final String username,
                                      @JsonProperty("secretKey") final String secretKey, @JsonProperty("validationCode") final int validationCode,
                                      @JsonProperty("scratchCodes") final List<Integer> scratchCodes,
                                      @JsonProperty("registrationDate") final ZonedDateTime registrationDate) {
        super(username, secretKey, validationCode, scratchCodes);
        setId(id);
        setRegistrationDate(registrationDate);
        this.cid = cid;
        this.rev = rev;
    }

    public CouchDbOneTimeTokenAccount(final OneTimeTokenAccount tokenAccount) {
        this(null, null, tokenAccount.getId(), tokenAccount.getUsername(), tokenAccount.getSecretKey(), tokenAccount.getValidationCode(),
            tokenAccount.getScratchCodes(), tokenAccount.getRegistrationDate());
    }

    /**
     * Update account info from account object.
     * @param account to be updated
     * @return this
     */
    public CouchDbOneTimeTokenAccount update(final OneTimeTokenAccount account) {
        setId(account.getId());
        setUsername(account.getUsername());
        setSecretKey(account.getSecretKey());
        setValidationCode(account.getValidationCode());
        setScratchCodes(account.getScratchCodes());
        setRegistrationDate(account.getRegistrationDate());
        return this;
    }
}
