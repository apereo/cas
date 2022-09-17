package org.apereo.cas.authentication.metadata;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DetailedCredentialMetaData;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic credential metadata implementation that stores the original credential ID and the original credential type.
 * This can be used as a simple converter for any {@link Credential} that doesn't implement {@link CredentialMetaData}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
@NoArgsConstructor(force = true)
@EqualsAndHashCode
public class BasicCredentialMetaData implements DetailedCredentialMetaData {

    @Serial
    private static final long serialVersionUID = 4929579849241505377L;

    /**
     * Credential type unique identifier.
     */
    private final String id;

    /**
     * Type of original credential.
     */
    private final Class<? extends Credential> credentialClass;

    private final Map<String, Serializable> properties = new HashMap<>();

    public BasicCredentialMetaData(final Credential credential, final Map<String, Serializable> properties) {
        this.id = credential.getId();
        this.credentialClass = credential.getClass();
        putProperties(properties);
    }

    public BasicCredentialMetaData(final Credential credential) {
        this(credential, new HashMap<>());
    }

    @Override
    public void putProperties(final Map<String, Serializable> properties) {
        this.properties.putAll(properties);
    }
}
