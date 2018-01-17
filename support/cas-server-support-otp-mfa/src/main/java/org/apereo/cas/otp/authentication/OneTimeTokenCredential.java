package org.apereo.cas.otp.authentication;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.Credential;
import java.io.Serializable;
import lombok.ToString;
import lombok.Getter;

/**
 * This is {@link OneTimeTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OneTimeTokenCredential implements Credential, Serializable {

    private static final long serialVersionUID = -7570600701132111037L;

    private String token;

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof OneTimeTokenCredential)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final OneTimeTokenCredential other = (OneTimeTokenCredential) obj;
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
}
