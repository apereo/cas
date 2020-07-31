package org.apereo.cas.couchdb.yubikey;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * This is {@link CouchDbYubiKeyAccount}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
@SuperBuilder
@Accessors(chain = true)
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
                                 @JsonProperty("devices") final @NonNull List<YubiKeyRegisteredDevice> devices,
                                 @JsonProperty("username") final @NonNull String username) {
        super(id, devices, username);
        this.cid = cid;
        this.rev = rev;
    }

    public CouchDbYubiKeyAccount(final List<YubiKeyRegisteredDevice> devices, final String username) {
        this(null, null, System.currentTimeMillis(), devices, username);
    }
}
