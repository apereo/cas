package org.apereo.cas.adaptors.generic.remote;

import org.apereo.cas.authentication.credential.AbstractCredential;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class RemoteAddressCredential extends AbstractCredential {

    /**
     * Serialization version marker.
     */
    private static final long serialVersionUID = -3638145328441211073L;

    private String remoteAddress;

    @JsonIgnore
    @Override
    public String getId() {
        return this.remoteAddress;
    }
}
