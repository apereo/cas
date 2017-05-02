package org.apereo.cas.adaptors.generic.remote;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apereo.cas.authentication.AbstractCredential;

/**
 * Represents a remote address as CAS credential.
 * @author Scott Battaglia
 * @since 3.2.1
 *
 */
public class RemoteAddressCredential extends AbstractCredential {

    /** Serialization version marker. */
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

    public String getRemoteAddress() {
        return this.remoteAddress;
    }

    @JsonIgnore
    @Override
    public String getId() {
        return this.remoteAddress;
    }
}
