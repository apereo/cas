package org.apereo.cas.adaptors.generic.remote;

import org.apereo.cas.authentication.credential.AbstractCredential;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import java.io.Serial;

/**
 * Represents a remote address as CAS credential.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RemoteAuthenticationCredential extends AbstractCredential {

    @Serial
    private static final long serialVersionUID = -3638145328441211073L;

    private String remoteAddress;

    private String cookie;

    public RemoteAuthenticationCredential(final String remoteAddress) {
        this(remoteAddress, null);
    }

    @JsonIgnore
    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(cookie, this.remoteAddress);
    }
}
