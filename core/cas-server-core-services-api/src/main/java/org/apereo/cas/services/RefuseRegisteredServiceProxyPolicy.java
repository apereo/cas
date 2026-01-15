package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;

/**
 * A proxy policy that disallows proxying.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RefuseRegisteredServiceProxyPolicy implements RegisteredServiceProxyPolicy {

    @Serial
    private static final long serialVersionUID = -5718445151129901484L;

    @JsonIgnore
    @Override
    public boolean isAllowedToProxy() {
        return false;
    }

    @JsonIgnore
    @Override
    public boolean isAllowedProxyCallbackUrl(final RegisteredService registeredService, final URL pgtUrl) {
        return false;
    }

}
