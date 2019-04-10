package org.apereo.cas.couchdb.u2f;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * This is {@link CouchDbU2FDeviceRegistration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
public class CouchDbU2FDeviceRegistration extends U2FDeviceRegistration {

    private static final long serialVersionUID = -5891058831508619021L;

    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbU2FDeviceRegistration(@JsonProperty("_id") final String cid,
                                        @JsonProperty("_rev") final String rev,
                                        @JsonProperty("id") final long id,
                                        @JsonProperty("username") final String username,
                                        @JsonProperty("record") final String record,
                                        @JsonProperty("createdDate") final LocalDate createdDate) {
        super(id, username, record, createdDate);
        this.cid = cid;
        this.rev = rev;
    }

    public CouchDbU2FDeviceRegistration(final U2FDeviceRegistration other) {
        this(null, null, other.getId(), other.getUsername(), other.getRecord(), other.getCreatedDate());
    }
}
