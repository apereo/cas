package org.apereo.cas.couchdb.trusted;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.ektorp.support.TypeDiscriminator;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * This is {@link CouchDbMultifactorAuthenticationTrustRecord}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
@TypeDiscriminator("doc.principal && doc.deviceFingerprint && doc.recordDate")
public class CouchDbMultifactorAuthenticationTrustRecord extends MultifactorAuthenticationTrustRecord {

    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbMultifactorAuthenticationTrustRecord(@JsonProperty("_id") final String cid,
                                                       @JsonProperty("_rev") final String rev,
                                                       @JsonProperty("id") final long id,
                                                       @JsonProperty("principal") final @NonNull String principal,
                                                       @JsonProperty("deviceFingerprint") final @NonNull String deviceFingerprint,
                                                       @JsonProperty("recordDate") final @NonNull ZonedDateTime recordDate,
                                                       @JsonProperty("expirationDate") final @NonNull Date expirationDate,
                                                       @JsonProperty("recordKey") final @NonNull String recordKey,
                                                       @JsonProperty("name") final String name) {
        this.cid = cid;
        this.rev = rev;
        setId(id);
        setPrincipal(principal);
        setDeviceFingerprint(deviceFingerprint);
        setRecordDate(recordDate);
        setRecordKey(recordKey);
        setName(name);
        setExpirationDate(expirationDate);
    }

    public CouchDbMultifactorAuthenticationTrustRecord(final MultifactorAuthenticationTrustRecord record) {
        this(null, null, record.getId(), record.getPrincipal(), record.getDeviceFingerprint(),
            record.getRecordDate(), record.getExpirationDate(), record.getRecordKey(), record.getName());
    }

    /**
     * Merge other record into this one for updating.
     *
     * @param other record to be merged into this one
     * @return this
     */
    public CouchDbMultifactorAuthenticationTrustRecord merge(final MultifactorAuthenticationTrustRecord other) {
        setId(other.getId());
        setPrincipal(other.getPrincipal());
        setDeviceFingerprint(other.getDeviceFingerprint());
        setRecordDate(other.getRecordDate());
        setRecordKey(other.getRecordKey());
        setName(other.getName());
        setExpirationDate(other.getExpirationDate());
        return this;
    }
}
