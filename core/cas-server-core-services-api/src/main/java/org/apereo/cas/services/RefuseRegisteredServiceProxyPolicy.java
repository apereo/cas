package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;

import java.net.URL;

/**
 * A proxy policy that disallows proxying.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
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
