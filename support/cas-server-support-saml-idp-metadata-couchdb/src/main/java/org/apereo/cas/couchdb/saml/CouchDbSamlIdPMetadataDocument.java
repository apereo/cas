package org.apereo.cas.couchdb.saml;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;

/**
 * This is {@link CouchDbSamlIdPMetadataDocument}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 * @deprecated Since 7
 */
@NoArgsConstructor
@Getter
@SuperBuilder
@Deprecated(since = "7.0.0")
public class CouchDbSamlIdPMetadataDocument extends SamlIdPMetadataDocument {
    @Serial
    private static final long serialVersionUID = 915217132219326234L;

    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbSamlIdPMetadataDocument(@JsonProperty("_id") final String cid,
                                          @JsonProperty("_rev") final String rev,
                                          @JsonProperty("id") final long id,
                                          @JsonProperty("metadata") final String metadata,
                                          @JsonProperty("signingCertificate") final String signingCertificate,
                                          @JsonProperty("signingKey") final String signingKey,
                                          @JsonProperty("encryptionCertificate") final String encryptionCertificate,
                                          @JsonProperty("encryptionKey") final String encryptionKey,
                                          @JsonProperty("appliesTo") final String appliesTo) {
        super(id, appliesTo, metadata, signingCertificate, signingKey, encryptionCertificate, encryptionKey);
        this.cid = cid;
        this.rev = rev;
    }

    public CouchDbSamlIdPMetadataDocument(final SamlIdPMetadataDocument doc) {
        super(doc.getId(), doc.getAppliesTo(), doc.getMetadata(), doc.getSigningCertificate(),
            doc.getSigningKey(), doc.getEncryptionCertificate(), doc.getEncryptionKey());
    }

    /**
     * Merge another doc into this one.
     * @param doc other doc
     * @return this
     */
    @CanIgnoreReturnValue
    public CouchDbSamlIdPMetadataDocument merge(final SamlIdPMetadataDocument doc) {
        setId(doc.getId());
        setMetadata(doc.getMetadata());
        setSigningCertificate(doc.getSigningCertificate());
        setSigningKey(doc.getSigningKey());
        setEncryptionCertificate(doc.getEncryptionCertificate());
        setEncryptionKey(doc.getEncryptionKey());
        setAppliesTo(doc.getAppliesTo());
        return this;
    }
}
