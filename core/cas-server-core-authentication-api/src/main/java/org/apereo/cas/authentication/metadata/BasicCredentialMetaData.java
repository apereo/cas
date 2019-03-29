package org.apereo.cas.authentication.metadata;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetaData;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Basic credential metadata implementation that stores the original credential ID and the original credential type.
 * This can be used as a simple converter for any {@link Credential} that doesn't implement {@link CredentialMetaData}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
@EqualsAndHashCode
public class BasicCredentialMetaData implements CredentialMetaData {

    /**
     * Serialization version marker.
     */
    private static final long serialVersionUID = 4929579849241505377L;

    private final Credential credential;

    @JsonCreator
    public BasicCredentialMetaData(final Credential credential) {
        this.credential = credential;
    }

    @JsonIgnore
    @Override
    public String getId() {
        return this.credential.getId();
    }

    @JsonIgnore
    @Override
    public Class<? extends Credential> getCredentialClass() {
        return this.credential.getClass();
    }
}
