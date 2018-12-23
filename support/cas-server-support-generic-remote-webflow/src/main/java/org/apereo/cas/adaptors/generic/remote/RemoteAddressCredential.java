package org.apereo.cas.adaptors.generic.remote;

import org.apereo.cas.authentication.credential.AbstractCredential;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Represents a remote address as CAS credential.
 *
 * @author Scott Battaglia
 * @since 3.2.1
 */
@Getter
public class RemoteAddressCredential extends AbstractCredential {

    /**
     * Serialization version marker.
     */
    private static final long serialVersionUID = -3638145328441211073L;

    private final String remoteAddress;

    /**
     * Instantiates a new remote address credential.
     *
     * @param remoteAddress the remote address
     */
    @JsonCreator
    public RemoteAddressCredential(@JsonProperty("remoteAddress") final String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @JsonIgnore
    @Override
    public String getId() {
        return this.remoteAddress;
    }
}
