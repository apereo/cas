package org.jasig.cas.authentication.token;

import com.google.common.io.ByteSource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;

/**
 * Defines a key to be used in encrypting/decrypting data. Keys can be
 * identified by a name.
 * @author Eric Pierce
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class TokenKey {
    private final String name;
    private final byte[] data;

    /**
     * Create a {@linkplain TokenKey} using a {@link String} for both the name
     * and the data.
     *
     * @param name The key name and data.
     */
    public TokenKey(@NotNull final String name) {
        this(name, name.getBytes());
    }

    /**
     * Create a {@linkplain TokenKey} with a given name and key data using strings.
     *
     * @param name The name of the key.
     * @param data A string to use for key data.
     */
    public TokenKey(@NotNull final String name, @NotNull final String data) {
        this(name, data.getBytes());
    }

    /**
     * Create a {@linkplain TokenKey} with a given name and key data.
     * @param name The name of the key.
     * @param data A byte array to use as the key data.
     */
    public TokenKey(@NotNull final String name, @NotNull final byte[] data) {
        this.name = name;
        this.data = data;
    }

    /** Get the key's data. */
    public ByteSource getData() {
        return ByteSource.wrap(this.data);
    }

    /** Get the key's name. */
    public String getName() {
        return this.name;
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
        final TokenKey rhs = (TokenKey) obj;
        return new EqualsBuilder()
                .append(this.name, rhs.name)
                .append(this.data, rhs.data)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(data)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .toString();
    }
}
