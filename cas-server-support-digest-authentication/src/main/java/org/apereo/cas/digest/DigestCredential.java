package org.apereo.cas.digest;

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
    private String realm;

    private String hash;

    private String id;

    /**
     * Instantiates a new Basic identifiable credental.
     *
     * @param uid   the id
     * @param realm the realm
     * @param hash  the hash
     */
    public DigestCredential(final String uid, final String realm, final String hash) {
        this.realm = realm;
        this.hash = hash;
        this.id = uid;
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
