package org.apereo.cas.authentication;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Basic credential metadata implementation that stores the original credential ID and the original credential type.
 * This can be used as a simple converter for any {@link Credential} that doesn't implement {@link CredentialMetaData}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class BasicCredentialMetaData implements CredentialMetaData, Serializable {

    /** Serialization version marker. */
    private static final long serialVersionUID = 4929579849241505377L;

    /** Credential type unique identifier. */
    private final String id;

    /** Type of original credential. */
    private Class<? extends Credential> credentialClass;

    /** No-arg constructor for serialization support. */
    private BasicCredentialMetaData() {
        this.id = null;
    }

    /**
     * Creates a new instance from the given credential.
     *
     * @param credential Credential for which metadata should be created.
     */
    public BasicCredentialMetaData(final Credential credential) {
        this.id = credential.getId();
        this.credentialClass = credential.getClass();
    }

    @Override
    public String getId() {
        return this.id;
    }


    @Override
    public Class<? extends Credential> getCredentialClass() {
        return this.credentialClass;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 21).append(this.id).append(this.credentialClass).toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof BasicCredentialMetaData)) {
            return false;
        }
        final BasicCredentialMetaData md = (BasicCredentialMetaData) other;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.id, md.id);
        builder.append(this.credentialClass, md.credentialClass);
        return builder.isEquals();
    }
}
