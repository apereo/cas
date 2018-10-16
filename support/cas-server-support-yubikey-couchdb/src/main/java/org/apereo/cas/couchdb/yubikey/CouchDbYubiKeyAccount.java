package org.apereo.cas.couchdb.yubikey;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * This is {@link CouchDbYubiKeyAccount}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public class CouchDbYubiKeyAccount extends YubiKeyAccount {

    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbYubiKeyAccount(@JsonProperty("_id") final String cid, //NOPMD
                                 @JsonProperty("_rev") final String rev,
                                 @JsonProperty("id") final long id,
                                 @JsonProperty("publicId") @NonNull final String publicId,
                                 @JsonProperty("username") @NonNull final String username) {
        super(id, publicId, username);
        this.cid = cid;
        this.rev = rev;
    }

    public CouchDbYubiKeyAccount(final String publicId, final String username) {
        this(null, null, -1, publicId, username);
    }

    public CouchDbYubiKeyAccount(final YubiKeyAccount account) {
        this(null, null, account.getId(), account.getPublicId(), account.getUsername());
    }
}
