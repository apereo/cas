package org.apereo.cas.couchdb.saml;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

/**
 * This is {@link CouchDbSamlIdPMetadataDocument}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CouchDbSamlIdPMetadataDocument extends SamlIdPMetadataDocument {
    @JsonProperty("_id")
    private String cid; //NOPMD

    @JsonProperty("_rev")
    private String rev; //NOPMD

    @JsonCreator
    public CouchDbSamlIdPMetadataDocument(@JsonProperty("_id") final String cid, //NOPMD
                                          @JsonProperty("_rev") final String rev,
                                          @JsonProperty("id") final BigInteger id,
                                          @JsonProperty("metadata") final String metadata,
                                          @JsonProperty("signingCertificate") final String signingCertificate,
                                          @JsonProperty("signingKey") final String signingKey,
                                          @JsonProperty("encryptionCertificate") final String encryptionCertificate,
                                          @JsonProperty("encryptionKey") final String encryptionKey) {
        super(id, metadata, signingCertificate, signingKey, encryptionCertificate, encryptionKey);
        this.cid = cid;
        this.rev = rev;
    }

    public CouchDbSamlIdPMetadataDocument() {
        super();
    }

    public CouchDbSamlIdPMetadataDocument(final SamlIdPMetadataDocument doc) {
        super(doc.getId(), doc.getMetadata(), doc.getSigningCertificate(), doc.getSigningKey(), doc.getEncryptionCertificate(), doc.getEncryptionKey());
    }

    /**
     * Merge another doc into this one.
     * @param doc other doc
     * @return this
     */
    public CouchDbSamlIdPMetadataDocument merge(final SamlIdPMetadataDocument doc) {
        setId(doc.getId());
        setMetadata(doc.getMetadata());
        setSigningCertificate(doc.getSigningCertificate());
        setSigningKey(doc.getSigningKey());
        setEncryptionCertificate(doc.getEncryptionCertificate());
        setEncryptionKey(doc.getEncryptionKey());
        return this;
    }
}
