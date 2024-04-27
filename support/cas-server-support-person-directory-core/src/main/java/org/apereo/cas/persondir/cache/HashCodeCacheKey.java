package org.apereo.cas.persondir.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import java.io.Serial;
import java.io.Serializable;

/**
 * Cache key which value is based on a pre-calculated hash code.
 *
 * @author Alex Ruiz
 * @since 7.1.0
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
class HashCodeCacheKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 3904677167731454262L;

    /**
     * Number that helps keep the uniqueness of this key.
     */
    private long checkSum;

    /**
     * Pre-calculated hash code.
     */
    private int hashCode;

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HashCodeCacheKey)) {
            return false;
        }
        val other = (HashCodeCacheKey) obj;
        if (checkSum != other.checkSum) {
            return false;
        }
        return hashCode == other.hashCode;
    }

    @Override
    public int hashCode() {
        return getHashCode();
    }
}
