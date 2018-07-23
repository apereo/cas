package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;

import java.net.URL;

/**
 * A proxy policy that disallows proxying.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@EqualsAndHashCode
public class RefuseRegisteredServiceProxyPolicy implements RegisteredServiceProxyPolicy {

    private static final long serialVersionUID = -5718445151129901484L;

    @JsonIgnore
    @Override
    public boolean isAllowedToProxy() {
        return false;
    }

    @JsonIgnore
    @Override
    public boolean isAllowedProxyCallbackUrl(final URL pgtUrl) {
        return false;
    }

}
