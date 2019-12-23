package org.apereo.cas.couchdb.yubikey;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link CouchDbYubiKeyAccount}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public class CouchDbYubiKeyAccount extends YubiKeyAccount {
    private static final long serialVersionUID = 2323614397554244567L;

    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbYubiKeyAccount(@JsonProperty("_id") final String cid,
                                 @JsonProperty("_rev") final String rev,
                                 @JsonProperty("id") final long id,
                                 @JsonProperty("deviceIdentifiers") final @NonNull ArrayList<String> deviceIdentifiers,
                                 @JsonProperty("username") final @NonNull String username) {
        super(id, deviceIdentifiers, username);
        this.cid = cid;
        this.rev = rev;
    }

    public CouchDbYubiKeyAccount(final Collection<String> publicId, final String username) {
        this(null, null, -1, new ArrayList<>(publicId), username);
    }

    public CouchDbYubiKeyAccount(final YubiKeyAccount account) {
        this(null, null, account.getId(), account.getDeviceIdentifiers(), account.getUsername());
    }
}
