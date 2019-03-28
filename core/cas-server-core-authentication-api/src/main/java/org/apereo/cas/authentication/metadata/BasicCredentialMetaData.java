package org.apereo.cas.authentication.metadata;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetaData;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Basic credential metadata implementation that stores the original credential ID and the original credential type.
 * This can be used as a simple converter for any {@link Credential} that doesn't implement {@link CredentialMetaData}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class BasicCredentialMetaData implements CredentialMetaData {

    /**
     * Serialization version marker.
     */
    private static final long serialVersionUID = 4929579849241505377L;

    private final Credential credential;

    @Override
    public String getId() {
        return this.credential.getId();
    }

    @Override
    public Class<? extends Credential> getCredentialClass() {
        return this.credential.getClass();
    }
}
