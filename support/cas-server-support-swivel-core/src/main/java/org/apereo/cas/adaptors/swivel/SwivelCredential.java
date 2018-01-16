package org.apereo.cas.adaptors.swivel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.Credential;
import java.io.Serializable;
import lombok.ToString;

/**
 * This is {@link SwivelCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString
public class SwivelCredential implements Credential, Serializable {

    private static final long serialVersionUID = 361318678073819595L;

    private String token;

    public SwivelCredential() {
    }

    public SwivelCredential(final String token) {
        this.token = token;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SwivelCredential)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final SwivelCredential other = (SwivelCredential) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.token, other.token);
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(97, 31);
        builder.append(this.token);
        return builder.toHashCode();
    }

    @Override
    public String getId() {
        return this.token;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(final String token) {
        this.token = token;
    }
}
