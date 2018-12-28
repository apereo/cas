package org.apereo.cas.couchdb.saml;

import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * This is {@link CouchDbSamlMetadataDocument}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public class CouchDbSamlMetadataDocument extends SamlMetadataDocument {
    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbSamlMetadataDocument(@JsonProperty("_id") final String cid,
                                       @JsonProperty("_rev") final String rev,
                                       @JsonProperty("id") final long id,
                                       @JsonProperty("name") final @NonNull String name,
                                       @JsonProperty("value") final String value,
                                       @JsonProperty("signature") final String signature) {
        super(id, name, value, signature);
        this.cid = cid;
        this.rev = rev;
    }

    public CouchDbSamlMetadataDocument(final SamlMetadataDocument document) {
        this(null, null, document.getId(), document.getName(), document.getValue(), document.getSignature());
    }

    /**
     * Merge other into this.
     * @param document other document to merge in
     * @return this
     */
    public CouchDbSamlMetadataDocument merge(final SamlMetadataDocument document) {
        setId(document.getId());
        setName(document.getName());
        setValue(document.getValue());
        setSignature(document.getSignature());
        return this;
    }
}
