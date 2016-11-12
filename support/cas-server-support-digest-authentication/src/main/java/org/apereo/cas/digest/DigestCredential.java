package org.apereo.cas.digest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.AbstractCredential;

/**
 * This is {@link DigestCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DigestCredential extends AbstractCredential {
    private static final long serialVersionUID = 1523693794392289803L;
    private String realm;

    private String hash;

    private String id;

    /**
     * Instantiates a new Basic identifiable credential.
     *
     * @param id   the id
     * @param realm the realm
     * @param hash  the hash
     */
    @JsonCreator
    public DigestCredential(@JsonProperty("id") final String id,
                            @JsonProperty("realm") final String realm,
                            @JsonProperty("hash") final String hash) {
        this.realm = realm;
        this.hash = hash;
        this.id = id;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final DigestCredential rhs = (DigestCredential) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.realm, rhs.realm)
                .append(this.hash, rhs.hash)
                .append(this.id, rhs.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(realm)
                .append(hash)
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("realm", realm)
                .append("hash", "[PROTECTED]")
                .append("id", this.id)
                .toString();
    }

    @Override
    public String getId() {
        return this.id;
    }
}
