package org.apereo.cas.authentication.metadata;

import module java.base;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetadata;
import org.apereo.cas.authentication.CredentialTrait;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * Basic credential metadata implementation that stores the original credential ID and the original credential type.
 * This can be used as a simple converter for any {@link Credential} that doesn't implement {@link CredentialMetadata}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Getter
@NoArgsConstructor(force = true)
@EqualsAndHashCode
@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@SuppressWarnings("NullAway.Init")
public class BasicCredentialMetadata implements CredentialMetadata {

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

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private final List<CredentialTrait> traits = new ArrayList<>();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private final Map<String, Serializable> properties = new HashMap<>();

    @Setter
    @Nullable
    private String tenant;

    public BasicCredentialMetadata(final Credential credential, final Map<String, ? extends Serializable> properties) {
        this.id = credential.getId();
        this.credentialClass = credential.getClass();
        putProperties(properties);
    }

    public BasicCredentialMetadata(final Credential credential) {
        this(credential, new HashMap<>());
    }

    @Override
    @CanIgnoreReturnValue
    public CredentialMetadata putProperties(final Map<String, ? extends Serializable> properties) {
        this.properties.putAll(properties);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public CredentialMetadata putProperty(final String key, final Serializable value) {
        this.properties.put(key, value);
        return this;
    }

    @Override
    public <T extends Serializable> @Nullable T getProperty(final String key, final Class<T> clazz) {
        return Optional.ofNullable(properties.get(key)).map(clazz::cast).orElse(null);
    }

    @Override
    @CanIgnoreReturnValue
    public CredentialMetadata addTrait(final CredentialTrait credentialTrait) {
        traits.add(credentialTrait);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public CredentialMetadata removeTrait(final Class<? extends CredentialTrait> clazz) {
        traits.removeIf(current -> current.getClass().equals(clazz));
        return this;
    }

    @Override
    @JsonIgnore
    public <T extends CredentialTrait> Optional<T> getTrait(final Class<T> clazz) {
        return traits
            .stream()
            .filter(trait -> clazz.equals(trait.getClass()))
            .map(clazz::cast)
            .findFirst();
    }

    @Override
    @JsonIgnore
    public boolean containsProperty(final String key) {
        return properties.containsKey(key);
    }

    @Override
    public void removeProperty(final String name) {
        this.properties.remove(name);
    }
}
