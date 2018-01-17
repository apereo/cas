package org.apereo.cas.adaptors.duo.authn;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.Credential;
import java.io.Serializable;
import lombok.ToString;
import lombok.Getter;

/**
 * Represents the duo credential.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DuoCredential implements Credential, Serializable {

    private static final long serialVersionUID = -7570600733132111037L;

    private String username;

    private String signedDuoResponse;

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DuoCredential)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final DuoCredential other = (DuoCredential) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.username, other.getUsername());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(97, 31);
        builder.append(this.username);
        return builder.toHashCode();
    }

    @Override
    public String getId() {
        return this.username;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.signedDuoResponse);
    }
}
