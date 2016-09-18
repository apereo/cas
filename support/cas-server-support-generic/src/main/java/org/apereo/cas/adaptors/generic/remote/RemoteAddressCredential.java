package org.apereo.cas.adaptors.generic.remote;

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

    private String remoteAddress;

    /**
     * Instantiates a new remote address credential.
     *
     * @param remoteAddress the remote address
     */
    public RemoteAddressCredential(final String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getRemoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public String getId() {
        return this.remoteAddress;
    }
}
